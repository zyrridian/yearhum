package com.yearhum.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yearhum.app.domain.model.ThemeMode
import com.yearhum.app.domain.model.UserSettings
import com.yearhum.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    private val repository: SettingsRepository,
) : ViewModel() {
    val settings: StateFlow<UserSettings> =
        repository.settings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    private val cacheCleared = Channel<Unit>(Channel.BUFFERED)

    /** Fires once each time the cache was cleared (drives a snackbar). */
    val cacheClearedEvents: Flow<Unit> = cacheCleared.receiveAsFlow()

    fun setBirthYear(year: Int?) = launch { repository.setBirthYear(year) }

    fun setCountry(code: String?) = launch { repository.setPreferredCountry(code) }

    fun setThemeMode(mode: ThemeMode) = launch { repository.setThemeMode(mode) }

    fun setDynamicColor(enabled: Boolean) = launch { repository.setDynamicColor(enabled) }

    fun setDecadeThemes(enabled: Boolean) = launch { repository.setDecadeThemes(enabled) }

    fun clearCache() = launch {
        repository.clearCachedMetadata()
        cacheCleared.send(Unit)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
