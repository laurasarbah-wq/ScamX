# Contributing

1. Create a branch and keep changes focused.
2. Add or update tests for every detection rule.
3. Use “risk indicators detected”; never accuse a named person of being a scammer.
4. Do not commit real personal data, suspicious live URLs, credentials, or moderator tokens.
5. Run `./gradlew :composeApp:testDebugUnitTest :composeApp:wasmJsBrowserDevelopmentExecutableDistribution :composeApp:assembleDebug` before opening a pull request.

Community-policy and Arabic-language changes require human review before release.
