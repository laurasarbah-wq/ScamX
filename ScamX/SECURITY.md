# Security policy

Please do not open public issues containing real suspicious messages, phone numbers, payment details, API keys, moderator tokens, or personal information. Report security concerns privately to the project owner.

Production deployments must keep `OPENAI_API_KEY`, `ADMIN_TOKEN`, `GOOGLE_SAFE_BROWSING_KEY`, and webhook URLs in server-side secrets. Never place them in the Kotlin app, website bundle, moderator JavaScript, repository, or QR code.
