package com.yearhum.app.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

const val USER_AGENT = "MusicTimeMachine/1.0 (contact@example.com)"
const val MUSICBRAINZ_MIN_INTERVAL_MS = 1_000L

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MusicBrainzClient

/** Ktor plugin that makes every request wait for a [RateLimiter] slot. */
class RateLimitConfig {
    var limiter: RateLimiter = RateLimiter(MUSICBRAINZ_MIN_INTERVAL_MS)
}

val RateLimitPlugin =
    createClientPlugin("RateLimit", ::RateLimitConfig) {
        val limiter = pluginConfig.limiter
        onRequest { _, _ -> limiter.acquire() }
    }

val AppJson =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(UserAgent) { agent = USER_AGENT }
        install(ContentNegotiation) { json(AppJson) }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 10_000
        }
    }

    @Provides
    @Singleton
    @MusicBrainzClient
    fun provideMusicBrainzClient(base: HttpClient): HttpClient = base.config {
        install(RateLimitPlugin) { limiter = RateLimiter(MUSICBRAINZ_MIN_INTERVAL_MS) }
    }
}
