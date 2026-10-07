package com.yearhum.app.domain.repository

import com.yearhum.app.core.common.AppResult
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.model.ReleaseGroupInfo
import com.yearhum.app.domain.model.ThemeMode
import com.yearhum.app.domain.model.UserSettings
import com.yearhum.app.domain.model.YearCapsule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface YearRepository {
    fun observeYears(): Flow<List<Int>>

    /** Emits null when the year is not part of the shipped dataset. */
    fun observeCapsule(year: Int): Flow<YearCapsule?>

    fun observeItem(id: Long): Flow<CapsuleItem?>
}

interface EnrichmentRepository {
    /** Cache-then-refresh: cached value (if any) first, network refresh when stale. */
    fun releaseGroup(mbid: String): Flow<AppResult<ReleaseGroupInfo>>
}

interface FavoritesRepository {
    fun observeIsFavorite(itemId: Long): Flow<Boolean>

    suspend fun toggle(itemId: Long)

    /** Saved items, most recently added first. */
    fun observeFavorites(): Flow<List<CapsuleItem>> = flowOf(emptyList())
}

interface SettingsRepository {
    val settings: Flow<UserSettings>

    /** Passing null clears the birth year. Either way the first-launch prompt counts as answered. */
    suspend fun setBirthYear(year: Int?)

    suspend fun completeOnboarding()

    suspend fun setPreferredCountry(code: String?)

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setDynamicColor(enabled: Boolean)

    suspend fun setDecadeThemes(enabled: Boolean)

    /** Clears cached MusicBrainz enrichment only; shipped capsule data stays. */
    suspend fun clearCachedMetadata()
}
