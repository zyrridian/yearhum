package com.yearhum.app.data.repository

import com.yearhum.app.core.datastore.UserPreferencesDataSource
import com.yearhum.app.data.local.dao.ReleaseGroupCacheDao
import com.yearhum.app.domain.model.ThemeMode
import com.yearhum.app.domain.model.UserSettings
import com.yearhum.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl
    @Inject
    constructor(
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
