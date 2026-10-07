package com.example.yearhum.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** Abstraction over wall-clock time so cache TTL logic is testable. */
fun interface TimeProvider {
    fun nowMillis(): Long
}
