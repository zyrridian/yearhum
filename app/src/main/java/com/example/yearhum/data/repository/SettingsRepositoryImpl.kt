package com.example.yearhum.data.repository

import com.example.yearhum.core.datastore.UserPreferencesDataSource
import com.example.yearhum.data.local.dao.ReleaseGroupCacheDao
import com.example.yearhum.domain.model.ThemeMode
import com.example.yearhum.domain.model.UserSettings
import com.example.yearhum.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val prefs: UserPreferencesDataSource,
    private val cache: ReleaseGroupCacheDao,
) : SettingsRepository {
    override val settings: Flow<UserSettings> = prefs.settings

    override suspend fun setBirthYear(year: Int?) {
        prefs.setBirthYear(year)
    }

    override suspend fun completeOnboarding() {
        prefs.completeOnboarding()
    }

    override suspend fun setPreferredCountry(code: String?) {
        prefs.setPreferredCountry(code)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.setThemeMode(mode)
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        prefs.setDynamicColor(enabled)
    }

    override suspend fun setDecadeThemes(enabled: Boolean) {
        prefs.setDecadeThemes(enabled)
    }

    /** Only the lazily fetched enrichment cache; the curated capsule data is never touched. */
    override suspend fun clearCachedMetadata() {
        cache.clear()
    }
}
