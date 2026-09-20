const EMIRATES = new Set([
  "Abu Dhabi", "Dubai", "Sharjah", "Ajman", "Umm Al Quwain", "Ras Al Khaimah", "Fujairah",
]);

const SCAM_TYPES = new Set([
  "Bank impersonation", "Delivery / customs", "Fake shopping", "Investment / crypto",
  "Job / task scam", "Government impersonation", "Account takeover", "Romance / extortion", "Other",
]);

function cors(env, request) {
  const origin = request?.headers.get("origin") || "";
  const allowed = String(env.ALLOWED_ORIGIN || "").split(",").map((item) => item.trim()).filter(Boolean);
  return {
    "Access-Control-Allow-Origin": allowed.includes(origin) ? origin : (allowed[0] || "https://example.invalid"),
    "Access-Control-Allow-Headers": "content-type, authorization",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Vary": "Origin",
  };
}

function json(env, request, body, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json; charset=utf-8", ...cors(env, request) },
  });
}

function redact(value) {
  return String(value || "")
    .replace(/\b(?:\+?971|0)?5\d(?:[ -]?\d){7}\b/g, "[phone removed]")
    .replace(/\b\d{4}[ -]?\d{4}[ -]?\d{4}[ -]?\d{4}\b/g, "[card removed]")
    .replace(/\b784[- ]?\d{4}[- ]?\d{7}[- ]?\d\b/g, "[ID removed]")
    .replace(/https?:\/\/\S+|www\.\S+/gi, "[link removed]")
    .replace(/\b[A-Z]{2}\d{2}[A-Z0-9]{11,30}\b/gi, "[account removed]")
    .replace(/[<>]/g, "")
    .trim()
    .slice(0, 600);
}

function localPolicyCheck(value) {
  const text = String(value || "");
  const reasons = [];
  if (/https?:\/\/|www\.|\[link removed\]/i.test(text)) reasons.push("Contains or originally contained a link");
  if (/\b(?:\+?971|0)?5\d(?:[ -]?\d){7}\b|\b\d{7,16}\b|\[(phone|card|ID|account) removed\]/i.test(text)) reasons.push("Contains or originally contained a phone, identity, card, or account number");
  if (/\b(idiot|stupid|bastard|kill|attack|hate)\b/i.test(text) || /(غبي|أحمق|سأقتلك|اقتله|أكره)/.test(text)) reasons.push("Aggressive or abusive language");
  if (/\b(Mr|Mrs|Ms|Dr)\.?\s+[A-Z][a-z]{2,}(?:\s+[A-Z][a-z]{2,})?\b/.test(text) || /\b[A-Z][a-z]{2,}\s+[A-Z][a-z]{2,}\s+(is|was)\s+(a\s+)?scammer\b/.test(text)) reasons.push("May identify or accuse a person by name");
  return reasons;
}

async function openAiModeration(text, env) {
  if (!env.OPENAI_API_KEY) return { status: "unavailable", categories: [], explanation: "OpenAI moderation is not configured" };
  const response = await fetch("https://api.openai.com/v1/moderations", {
    method: "POST",
    headers: { "content-type": "application/json", authorization: `Bearer ${env.OPENAI_API_KEY}` },
    body: JSON.stringify({ model: "omni-moderation-latest", input: text }),
  });
  if (!response.ok) return { status: "unavailable", categories: [], explanation: "OpenAI moderation could not be reached" };
  const payload = await response.json();
  const result = payload.results?.[0];
  const categories = Object.entries(result?.categories || {}).filter(([, flagged]) => flagged).map(([name]) => name);
  return {
    status: result?.flagged ? "flagged" : "green",
    categories,
    explanation: result?.flagged ? `OpenAI flagged: ${categories.join(", ")}` : "No harmful-content category was flagged by OpenAI",
  };
}

function extractUrl(value) {
  const match = String(value || "").match(/https?:\/\/[^\s<>"']+/i);
  return match ? match[0] : null;
}

async function safeBrowsing(url, env) {
  if (!url || !env.GOOGLE_SAFE_BROWSING_KEY) return { status: "unavailable", matches: [] };
  const endpoint = new URL("https://safebrowsing.googleapis.com/v5/urls:search");
  endpoint.searchParams.append("urls", url);
  endpoint.searchParams.set("key", env.GOOGLE_SAFE_BROWSING_KEY);
  const response = await fetch(endpoint, { headers: { accept: "application/json" } });
  if (!response.ok) return { status: "unavailable", matches: [] };
  const payload = await response.json();
  return { status: "checked", matches: payload.threats || [] };
}

async function handleCheck(request, env) {
  const body = await request.json().catch(() => ({}));
  const input = String(body.input || "").trim().slice(0, 2048);
  if (!input) return json(env, request, { error: "Input is required" }, 400);
  const url = extractUrl(input);
  const reputation = body.onlineConsent === true ? await safeBrowsing(url, env) : { status: "not_requested", matches: [] };
  return json(env, request, { reputation, knownThreat: reputation.matches.length > 0, checkedAt: new Date().toISOString(), privacy: "Input not stored" });
}

async function listReports(request, env, url) {
  const emirate = url.searchParams.get("emirate");
  const type = url.searchParams.get("type");
  let sql = "SELECT id, emirate, scam_type AS scamType, notes_redacted AS notes, created_at AS createdAt FROM community_reports WHERE status = 'approved'";
  const values = [];
  if (emirate && EMIRATES.has(emirate)) { sql += " AND emirate = ?"; values.push(emirate); }
  if (type && SCAM_TYPES.has(type)) { sql += " AND scam_type = ?"; values.push(type); }
  sql += " ORDER BY created_at DESC LIMIT 100";
  const result = await env.DB.prepare(sql).bind(...values).all();
  return json(env, request, { reports: result.results || [] });
}

async function notifyModerators(env, report) {
  if (!env.MODERATOR_WEBHOOK_URL) return;
  await fetch(env.MODERATOR_WEBHOOK_URL, {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: JSON.stringify({ text: `ScamX moderation request ${report.id}: ${report.aiStatus}. ${report.reasons.join("; ") || "No obvious policy issue"}`, reportId: report.id }),
  }).catch(() => null);
}

async function createReport(request, env) {
  const body = await request.json().catch(() => ({}));
  const emirate = String(body.emirate || "");
  let scamType = String(body.scamType || "");
  if (!EMIRATES.has(emirate)) return json(env, request, { error: "Invalid emirate" }, 400);
  if (body.aiConsent !== true) return json(env, request, { error: "Moderation consent is required" }, 400);
  if (scamType === "Other") scamType = redact(body.customType).slice(0, 60);
  else if (!SCAM_TYPES.has(scamType)) scamType = redact(scamType).slice(0, 60);
  const rawNotes = String(body.notes || "").slice(0, 1000);
  const policyReasons = localPolicyCheck(rawNotes);
  const notes = redact(rawNotes);
  if (notes.length < 20 || scamType.length < 3) return json(env, request, { error: "A clearer description is required" }, 400);

  // Only redacted text is sent to OpenAI and stored. AI assists triage; it never publishes a post.
  const ai = await openAiModeration(notes, env);
  const reasons = [...policyReasons, ...(ai.status === "flagged" ? [ai.explanation] : [])];
  const aiStatus = reasons.length ? "flagged" : ai.status;
  const id = crypto.randomUUID();
  const createdAt = new Date().toISOString();
  await env.DB.prepare(`INSERT INTO community_reports
    (id, emirate, scam_type, notes_redacted, status, ai_status, moderation_reasons, moderation_categories, created_at)
    VALUES (?, ?, ?, ?, 'pending', ?, ?, ?, ?)`)
    .bind(id, emirate, scamType, notes, aiStatus, JSON.stringify(reasons), JSON.stringify(ai.categories), createdAt).run();
  await notifyModerators(env, { id, aiStatus, reasons });
  return json(env, request, { id, status: "pending", aiStatus, moderationReasons: reasons, message: "Queued for human moderation" }, 202);
}

function requireAdmin(request, env) {
  const token = request.headers.get("authorization")?.replace(/^Bearer\s+/i, "");
  return Boolean(env.ADMIN_TOKEN && token === env.ADMIN_TOKEN);
}

async function listPending(request, env, url) {
  if (!requireAdmin(request, env)) return json(env, request, { error: "Unauthorized" }, 401);
  const status = url.searchParams.get("status") || "pending";
  if (!["pending", "approved", "rejected"].includes(status)) return json(env, request, { error: "Invalid status" }, 400);
  const result = await env.DB.prepare(`SELECT id, emirate, scam_type AS scamType, notes_redacted AS notes,
    status, ai_status AS aiStatus, moderation_reasons AS moderationReasons,
    moderation_categories AS moderationCategories, created_at AS createdAt, moderated_at AS moderatedAt
    FROM community_reports WHERE status = ? ORDER BY created_at DESC LIMIT 200`).bind(status).all();
  return json(env, request, { reports: result.results || [] });
}

async function moderate(request, env, id) {
  if (!requireAdmin(request, env)) return json(env, request, { error: "Unauthorized" }, 401);
  const body = await request.json().catch(() => ({}));
  if (!new Set(["approved", "rejected"]).has(body.action)) return json(env, request, { error: "Invalid action" }, 400);
  const now = new Date().toISOString();
  const moderatorNote = String(body.note || "").replace(/[<>]/g, "").slice(0, 300);
  const updated = await env.DB.prepare("UPDATE community_reports SET status = ?, moderator_note = ?, moderated_at = ? WHERE id = ? AND status = 'pending'")
    .bind(body.action, moderatorNote, now, id).run();
  if (!updated.meta?.changes) return json(env, request, { error: "Pending report not found" }, 404);
  await env.DB.prepare("INSERT INTO moderation_events (report_id, action, note, created_at) VALUES (?, ?, ?, ?)")
    .bind(id, body.action, moderatorNote, now).run();
  return json(env, request, { id, status: body.action });
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: cors(env, request) });
    const url = new URL(request.url);
    try {
      if (request.method === "GET" && url.pathname === "/api/health") return json(env, request, { ok: true, aiModeration: Boolean(env.OPENAI_API_KEY) });
      if (request.method === "POST" && url.pathname === "/api/check") return handleCheck(request, env);
      if (request.method === "GET" && url.pathname === "/api/community") return listReports(request, env, url);
      if (request.method === "POST" && url.pathname === "/api/community") return createReport(request, env);
      if (request.method === "GET" && url.pathname === "/api/admin/reports") return listPending(request, env, url);
      const moderation = url.pathname.match(/^\/api\/admin\/reports\/([a-f0-9-]+)$/i);
      if (request.method === "POST" && moderation) return moderate(request, env, moderation[1]);
      return json(env, request, { error: "Not found" }, 404);
    } catch (error) {
      console.error(error);
      return json(env, request, { error: "Request failed" }, 500);
    }
  },
};
