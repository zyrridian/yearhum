package com.yearhum.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yearhum.app.domain.model.ThemeMode
import com.yearhum.app.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataSource
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val store = context.userDataStore

        val settings: Flow<UserSettings> =
            store.data
                .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
                .map { prefs ->
                    UserSettings(
                        birthYear = prefs[BIRTH_YEAR],
                        onboardingDone = prefs[ONBOARDING_DONE] ?: false,
                        preferredCountry = prefs[COUNTRY],
                        themeMode =
                            prefs[THEME_MODE]
                                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                                ?: ThemeMode.SYSTEM,
                        dynamicColor = prefs[DYNAMIC_COLOR] ?: true,
                        decadeThemes = prefs[DECADE_THEMES] ?: true,
                    )
                }

        suspend fun setBirthYear(year: Int?) =
            store.edit {
                if (year == null) it.remove(BIRTH_YEAR) else it[BIRTH_YEAR] = year
                it[ONBOARDING_DONE] = true
            }

        suspend fun completeOnboarding() = store.edit { it[ONBOARDING_DONE] = true }

        suspend fun setPreferredCountry(code: String?) =
            store.edit {
                if (code == null) it.remove(COUNTRY) else it[COUNTRY] = code
            }

        suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[THEME_MODE] = mode.name }

        suspend fun setDynamicColor(enabled: Boolean) = store.edit { it[DYNAMIC_COLOR] = enabled }

        suspend fun setDecadeThemes(enabled: Boolean) = store.edit { it[DECADE_THEMES] = enabled }

        private companion object {
            val BIRTH_YEAR = intPreferencesKey("birth_year")
            val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
            val COUNTRY = stringPreferencesKey("preferred_country")
            val THEME_MODE = stringPreferencesKey("theme_mode")
            val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
            val DECADE_THEMES = booleanPreferencesKey("decade_themes")
        }
    }
