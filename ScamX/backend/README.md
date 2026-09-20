# ScamX API and community database

This Cloudflare Worker + D1 service gives ScamX a shared, moderated community database, AI-assisted triage, moderator notifications, and an optional online URL-reputation check.

## Privacy and trust boundaries

- The complete pasted message is never stored by the API.
- Online URL reputation is used only after the user explicitly opts in. Only the extracted URL is sent to Google Safe Browsing.
- Community submissions are anonymous and pass through server-side redaction before storage.
- New community submissions remain `pending`; only an authenticated moderator can approve them.
- Deterministic policy checks detect links, numbers, abusive language, and possible named accusations before storage.
- Only redacted community text is sent to OpenAI's `omni-moderation-latest` endpoint. AI labels a post green or flagged and explains relevant categories; it never publishes or rejects a post.
- Approved community reports remain labelled as community information, not proof against a person or phone-number owner.
- Do not use Reddit posts as verified records. They may inform moderation research, but an official source or technical reputation service is required for a verified label.

## Setup

1. Install Wrangler, sign in to Cloudflare, and create a D1 database named `scamx-community`.
2. Copy `wrangler.toml.example` to `wrangler.toml`, then add the returned database ID and the public website origin.
3. Apply `schema.sql` to the remote D1 database.
4. Add a strong `ADMIN_TOKEN` secret.
5. Add `OPENAI_API_KEY` as a Worker secret to enable AI-assisted community moderation.
6. Optional: add `MODERATOR_WEBHOOK_URL` to notify the development/moderation team of a new queue item. Notifications contain only the report ID and flag summary.
7. Optional: add a server-side `GOOGLE_SAFE_BROWSING_KEY` secret. Check Google's current usage terms; Safe Browsing is for non-commercial use, while commercial products should evaluate Web Risk.
8. Deploy the Worker and set its public URL in the ScamX client configuration.
9. Host `moderator/` separately, then enter the API URL and `ADMIN_TOKEN` in its login panel. The token remains only in the current browser tab.

If upgrading an existing D1 database, apply `migrations/0002_ai_moderation.sql` instead of recreating the schema.

Before public launch, add automated abuse throttling or Cloudflare Turnstile, a moderator dashboard, retention limits, deletion procedures, and legal/privacy review for the UAE.
