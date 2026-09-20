# ScamX

**Pause. Check. Protect.**  
**توقّف. تحقّق. احمِ نفسك.**

ScamX is a privacy-friendly checker for suspicious messages, links, and phone numbers. It uses a shared Kotlin risk engine and shared Compose Multiplatform interface for Android and the web. Message analysis happens on the device. If the user explicitly enables the optional online reputation check, only an extracted URL is sent—never the pasted message.
# Website Preview 1
<img width="1919" height="900" alt="Screenshot 2026-09-20 122558" src="https://github.com/user-attachments/assets/a72b0db4-63ee-4b1d-836b-a02998f8f1c7" />

# Website Preview 2
<img width="1910" height="836" alt="Screenshot 2026-09-20 122625" src="https://github.com/user-attachments/assets/5d339f65-b76b-4486-8e19-8b0cee1c2d0b" />

# Moderator website preview
<img width="1617" height="846" alt="image (1)" src="https://github.com/user-attachments/assets/83a0a7f1-18e7-4483-91d1-cef7f97992ba" />



The final v1.2 release includes:

- Home, official reporting, and moderated community menus.
- Expanded UAE-focused patterns for delivery fees, task/job scams, bank and government impersonation, remote-access requests, personal-account payments, and deceptive links.
- Optional online URL reputation checking. The complete message stays on the device; only an extracted URL is sent after explicit consent.
- A Cloudflare Worker + D1 backend in `backend/` for moderated anonymous submissions and server-side Google Safe Browsing integration.
- AI-assisted community triage using OpenAI moderation, deterministic privacy/policy checks, optional moderator webhook notifications, and mandatory human approval.
- A separate responsive moderator console in `moderator/`.
- Quick examples, highlighted matched phrases, risk meter, copy/clear/report actions, feedback controls, filters, a score explanation, and automated rule tests.

## Open in IntelliJ IDEA

1. Install IntelliJ IDEA with the **Kotlin Multiplatform** and **Android** plugins.
2. Open this folder (the folder containing `settings.gradle.kts`).
3. Select JDK 17 or JDK 21 as the Gradle JVM and allow Gradle to sync.
4. Run **composeApp [wasmJs]** for the website, or the **composeApp** Android configuration with an emulator/device.

## Build

- Website: run the Gradle task `composeApp > Tasks > kotlin browser > wasmJsBrowserDistribution`.
- Android test APK: run `composeApp > Tasks > build > assembleDebug`.

The production website files are generated in `composeApp/build/dist/wasmJs/productionExecutable`. The APK is generated under `composeApp/build/outputs/apk/debug`.

If IntelliJ has no Android SDK configured, open **File > Project Structure > SDKs**, add an Android SDK with API 35, and let IntelliJ install the requested platform and build tools. Android Studio is not required for this project.

## Enable the shared database and online check

Deploy the service described in `backend/README.md`, then set `SCAMX_API_BASE_URL` in `composeApp/src/commonMain/kotlin/ae/scamx/app/ScamXApi.kt` to its HTTPS URL and rebuild. API keys remain server-side.

The moderation flow is: anonymous submission → local warning preview → server-side redaction → OpenAI moderation → pending D1 record → optional moderator notification → human approve/reject in `moderator/`. AI never publishes content automatically.

The OpenAI key must be configured as the Worker secret `OPENAI_API_KEY`. Submitted community text is redacted before it is sent to the moderation endpoint. The app displays explicit consent before submission.

## Moderator console

Host the static files in `moderator/` on an access-controlled HTTPS site. Enter the deployed API URL and moderator token to view pending, approved, or rejected reports. Each card shows the redacted comment, AI status, flag reasons, and human approval/rejection controls.

Do not expose the moderator console publicly without an identity-aware access layer. The included token gate is an MVP control, not a replacement for production authentication.

## GitHub and verification

The repository includes a GitHub Actions workflow that compiles, tests, and packages the Android and web targets. See `CONTRIBUTING.md` and `SECURITY.md` before publishing.

## QR codes

Generate the final website and Android QR codes only after publishing both files at permanent HTTPS URLs. A localhost QR code will not work for other users. Do not point the Android QR directly to an unsigned or changing build; publish a clearly labelled test APK download page or an app-store testing link.

After deployment, run `pip install -r tools/requirements.txt` and then `python tools/generate_qr.py --website https://... --android https://...`. The script refuses non-HTTPS release URLs.

## Important limitations

ScamX detects common risk indicators. It does not prove that a sender is a scammer, and a low-risk result does not guarantee safety. Before publishing, check the ScamX name, domain, logo, and app-store availability, and have the rules and Arabic wording reviewed locally.

The bundled Noto Sans Arabic font is licensed under the SIL Open Font License; see `OFL-NotoSansArabic.txt`.
