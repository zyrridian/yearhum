package com.example.yearhum.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Spaces calls at least [minIntervalMs] apart (MusicBrainz allows ~1 request/second).
 * Time and delay are injectable so tests can run on virtual time.
 */
class RateLimiter(
    private val minIntervalMs: Long,
    private val now: () -> Long = { System.nanoTime() / NANOS_PER_MILLI },
    private val sleep: suspend (Long) -> Unit = { delay(it) },
) {
    private val mutex = Mutex()
    private var lastStart: Long? = null

    suspend fun acquire() {
        mutex.withLock {
            val last = lastStart
            if (last != null) {
                val wait = minIntervalMs - (now() - last)
                if (wait > 0) sleep(wait)
            }
            lastStart = now()
        }
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
