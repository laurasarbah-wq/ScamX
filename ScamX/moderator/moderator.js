const $ = (selector, root = document) => root.querySelector(selector);
const queue = $("#queue");
const message = $("#message");

function config() {
  return { api: $("#api").value.trim().replace(/\/$/, ""), token: $("#token").value.trim() };
}

function parseList(value) {
  if (Array.isArray(value)) return value;
  try { return JSON.parse(value || "[]"); } catch { return []; }
}

async function request(path, options = {}) {
  const { api, token } = config();
  if (!api.startsWith("https://") || !token) throw new Error("Enter the HTTPS API URL and moderator token.");
  const response = await fetch(`${api}${path}`, { ...options, headers: { "content-type": "application/json", authorization: `Bearer ${token}`, ...(options.headers || {}) } });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.error || `Request failed (${response.status})`);
  return data;
}

function renderReport(report) {
  const node = $("#card-template").content.cloneNode(true);
  const article = $(".report", node);
  const badge = $(".ai-badge", node);
  badge.textContent = report.aiStatus === "green" ? "● GREEN — REVIEW" : report.aiStatus === "flagged" ? "⚑ FLAGGED" : "● AI UNAVAILABLE";
  badge.classList.add(`ai-${report.aiStatus || "unavailable"}`);
  $("time", node).textContent = new Date(report.createdAt).toLocaleString();
  $(".type", node).textContent = report.scamType;
  $(".meta", node).textContent = `${report.emirate} • Anonymous submission • ${report.status}`;
  $(".notes", node).textContent = report.notes;
  const reasons = [...parseList(report.moderationReasons), ...parseList(report.moderationCategories).map((item) => `OpenAI category: ${item}`)];
  if (!reasons.length) reasons.push("No obvious policy issue detected; human context review is still required.");
  const list = $(".reasons ul", node);
  reasons.forEach((reason) => { const item = document.createElement("li"); item.textContent = reason; list.append(item); });
  if (report.status !== "pending") $(".actions", node).remove();
  else {
    $(".approve", node).addEventListener("click", () => decide(report.id, "approved", article));
    $(".reject", node).addEventListener("click", () => decide(report.id, "rejected", article));
  }
  return node;
}

async function loadQueue() {
  message.textContent = "Loading…"; queue.replaceChildren();
  try {
    const data = await request(`/api/admin/reports?status=${encodeURIComponent($("#status").value)}`);
    data.reports.forEach((report) => queue.append(renderReport(report)));
    $("#count").textContent = `${data.reports.length} report${data.reports.length === 1 ? "" : "s"}`;
    message.textContent = data.reports.length ? "" : "No reports in this queue.";
  } catch (error) { message.textContent = error.message; }
}

async function decide(id, action, article) {
  const note = $("textarea", article).value;
  try {
    await request(`/api/admin/reports/${id}`, { method: "POST", body: JSON.stringify({ action, note }) });
    article.remove();
    message.textContent = `Report ${action}.`;
    const remaining = queue.children.length;
    $("#count").textContent = `${remaining} report${remaining === 1 ? "" : "s"}`;
  } catch (error) { message.textContent = error.message; }
}

$("#load").addEventListener("click", loadQueue);
