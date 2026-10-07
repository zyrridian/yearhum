package builder

import java.io.File
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.Duration

const val USER_AGENT = "MusicTimeMachine-DatasetBuilder/1.0 (contact@example.com)"

fun String.urlEncode(): String = URLEncoder.encode(this, Charsets.UTF_8)

/**
 * Polite HTTP client: identifies itself, spaces requests per host, retries on 429/503,
 * and caches every successful response on disk so reruns are free and resumable.
 */
class CachedHttp(private val cacheDir: File, private val minIntervalMs: Map<String, Long>) {
    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(15))
        .build()
    private val lastCall = mutableMapOf<String, Long>()

    init {
        cacheDir.mkdirs()
    }

    /** Returns the body, or null on 404. Throws for other persistent failures. */
    fun get(url: String): String? {
        val file = File(cacheDir, sha(url) + ".txt")
        if (file.exists()) return file.readText().takeIf { it != NOT_FOUND }
        repeat(MAX_ATTEMPTS) { attempt ->
            throttle(URI(url).host)
            val request = HttpRequest.newBuilder(URI(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build()
            val response = try {
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: Exception) {
                System.err.println("  network error (${e.message}), retrying")
                Thread.sleep(BACKOFF_MS * (attempt + 1))
                return@repeat
            }
            when (response.statusCode()) {
                200 -> return response.body().also { file.writeText(it) }
                404 -> {
                    file.writeText(NOT_FOUND)
                    return null
                }
                429, 503 -> Thread.sleep(BACKOFF_MS * (attempt + 1))
                else -> {
                    System.err.println("HTTP ${response.statusCode()} for $url")
                    return null
                }
            }
        }
        System.err.println("  giving up on $url after $MAX_ATTEMPTS attempts")
        return null
    }

    private fun throttle(host: String) {
        val interval = minIntervalMs[host] ?: DEFAULT_INTERVAL_MS
        val wait = interval - (System.currentTimeMillis() - (lastCall[host] ?: 0))
        if (wait > 0) Thread.sleep(wait)
        lastCall[host] = System.currentTimeMillis()
    }

    private fun sha(s: String) = MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
        .joinToString("") { "%02x".format(it) }.take(HASH_LENGTH)

    private companion object {
        const val NOT_FOUND = "__404__"
        const val MAX_ATTEMPTS = 6
        const val BACKOFF_MS = 5_000L
        const val DEFAULT_INTERVAL_MS = 200L
        const val HASH_LENGTH = 32
    }
}
