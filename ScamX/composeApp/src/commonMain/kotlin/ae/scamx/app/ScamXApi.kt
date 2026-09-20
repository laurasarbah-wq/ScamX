package ae.scamx.app

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent

// Set this after deploying the Worker in /backend. Keep API keys on the Worker, never here.
const val SCAMX_API_BASE_URL = ""

data class OnlineCheckResult(val state: String, val knownThreat: Boolean = false)

object ScamXApi {
    private val client = HttpClient(CIO)

    val configured: Boolean get() = SCAMX_API_BASE_URL.startsWith("https://")

    suspend fun check(input: String): OnlineCheckResult {
        if (!configured) return OnlineCheckResult("not_configured")
        // Extract on-device so the pasted message never leaves the user's device.
        val url = Regex("""https?://[^\s<>\"']+""", RegexOption.IGNORE_CASE)
            .find(input)
            ?.value
            ?: return OnlineCheckResult("no_url")
        return try {
            val response = client.post("${SCAMX_API_BASE_URL.trimEnd('/')}/api/check") {
                setBody(TextContent("{\"input\":${jsonString(url)},\"onlineConsent\":true}", ContentType.Application.Json))
            }
            val body = response.bodyAsText()
            when {
                "\"knownThreat\":true" in body -> OnlineCheckResult("matched", true)
                "\"status\":\"checked\"" in body -> OnlineCheckResult("clear")
                else -> OnlineCheckResult("unavailable")
            }
        } catch (_: Throwable) {
            OnlineCheckResult("unavailable")
        }
    }

    suspend fun submit(post: CommunityPost): String {
        if (!configured) return "not_configured"
        return try {
            val body = """{"emirate":${jsonString(post.emirate)},"scamType":${jsonString(post.scamType)},"notes":${jsonString(post.notes)},"aiConsent":true}"""
            val response = client.post("${SCAMX_API_BASE_URL.trimEnd('/')}/api/community") {
                setBody(TextContent(body, ContentType.Application.Json))
            }
            if (response.status.value !in 200..299) return "failed"
            val responseBody = response.bodyAsText()
            when {
                "\"aiStatus\":\"flagged\"" in responseBody -> "sent_flagged"
                "\"aiStatus\":\"green\"" in responseBody -> "sent_green"
                else -> "sent_unavailable"
            }
        } catch (_: Throwable) {
            "failed"
        }
    }

    private fun jsonString(value: String): String = buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
}
