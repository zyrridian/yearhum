package com.example.yearhum.data.repository

import com.example.yearhum.core.common.AppResult
import com.example.yearhum.core.common.TimeProvider
import com.example.yearhum.data.local.dao.CapsuleDao
import com.example.yearhum.data.local.dao.FavoriteDao
import com.example.yearhum.data.local.dao.ReleaseGroupCacheDao
import com.example.yearhum.data.local.entity.FavoriteEntity
import com.example.yearhum.data.mapper.toDomain
import com.example.yearhum.data.mapper.toEntity
import com.example.yearhum.data.remote.musicbrainz.MusicBrainzApi
import com.example.yearhum.domain.model.CapsuleItem
import com.example.yearhum.domain.model.ReleaseGroupInfo
import com.example.yearhum.domain.model.YearCapsule
import com.example.yearhum.domain.repository.EnrichmentRepository
import com.example.yearhum.domain.repository.FavoritesRepository
import com.example.yearhum.domain.repository.YearRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YearRepositoryImpl @Inject constructor(
    private val dao: CapsuleDao,
) : YearRepository {
    override fun observeYears(): Flow<List<Int>> = dao.observeYears()

    override fun observeCapsule(year: Int): Flow<YearCapsule?> =
        combine(dao.observeCapsule(year), dao.observeItems(year)) { capsule, items ->
            capsule?.toDomain(items)
        }

    override fun observeItem(id: Long): Flow<CapsuleItem?> =
        dao.observeItem(id).map { it?.toDomain() }
}

@Singleton
class EnrichmentRepositoryImpl @Inject constructor(
    private val api: MusicBrainzApi,
    private val cache: ReleaseGroupCacheDao,
    private val time: TimeProvider,
) : EnrichmentRepository {
    override fun releaseGroup(mbid: String): Flow<AppResult<ReleaseGroupInfo>> = flow {
        val cached = cache.get(mbid)
        val fresh = cached != null && time.nowMillis() - cached.fetchedAt < TTL_MS
        if (cached != null) emit(AppResult.Success(cached.toDomain()))
        if (fresh) return@flow
        when (val remote = api.releaseGroup(mbid)) {
            is AppResult.Success -> {
                val entity = remote.value.toEntity(time.nowMillis())
                cache.upsert(entity)
                emit(AppResult.Success(entity.toDomain()))
            }
            // Stale cache is better than an error; only surface failures when we have nothing.
            is AppResult.Failure -> if (cached == null) emit(remote)
        }
    }

    companion object {
        const val TTL_MS = 30L * 24 * 60 * 60 * 1000
    }
}

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
    private val time: TimeProvider,
) : FavoritesRepository {
    override fun observeIsFavorite(itemId: Long): Flow<Boolean> = dao.observeIsFavorite(itemId)

    override suspend fun toggle(itemId: Long) {
        if (dao.add(FavoriteEntity(itemId, time.nowMillis())) == -1L) dao.remove(itemId)
    }

    override fun observeFavorites(): Flow<List<CapsuleItem>> =
        dao.observeFavoriteItems().map { items -> items.mapNotNull { it.toDomain() } }
}
