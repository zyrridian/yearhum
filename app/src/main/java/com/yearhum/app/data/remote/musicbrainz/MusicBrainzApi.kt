package com.yearhum.app.data.remote.musicbrainz

import com.yearhum.app.core.common.AppError
import com.yearhum.app.core.common.AppResult
import com.yearhum.app.core.network.MusicBrainzClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ArtistCreditDto(
    val name: String,
)

@Serializable
data class ReleaseGroupDto(
    val id: String,
    val title: String,
    @SerialName("first-release-date") val firstReleaseDate: String? = null,
    @SerialName("primary-type") val primaryType: String? = null,
    @SerialName("artist-credit") val artistCredit: List<ArtistCreditDto> = emptyList(),
)

interface MusicBrainzApi {
    suspend fun releaseGroup(mbid: String): AppResult<ReleaseGroupDto>
}

@Singleton
class KtorMusicBrainzApi
    @Inject
    constructor(
        @MusicBrainzClient private val client: HttpClient,
    ) : MusicBrainzApi {
        override suspend fun releaseGroup(mbid: String): AppResult<ReleaseGroupDto> =
            try {
                val response =
                    client.get("https://musicbrainz.org/ws/2/release-group/$mbid") {
                        parameter("inc", "artist-credits")
                        parameter("fmt", "json")
                    }
                when {
                    response.status == HttpStatusCode.NotFound -> {
                        AppResult.Failure(AppError.NotFound)
                    }

                    response.status == HttpStatusCode.TooManyRequests ||
                        response.status == HttpStatusCode.ServiceUnavailable -> {
                        AppResult.Failure(AppError.RateLimited)
                    }

                    response.status.value >= SERVER_ERROR -> {
                        AppResult.Failure(AppError.Server(response.status.value))
                    }

                    response.status.value in HTTP_OK_RANGE -> {
                        AppResult.Success(response.body<ReleaseGroupDto>())
                    }

                    else -> {
                        AppResult.Failure(AppError.Server(response.status.value))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                AppResult.Failure(AppError.Offline)
            } catch (e: Exception) {
                AppResult.Failure(AppError.Unknown(e.message))
            }

        private companion object {
            const val SERVER_ERROR = 500
            val HTTP_OK_RANGE = 200..299
        }
    }
