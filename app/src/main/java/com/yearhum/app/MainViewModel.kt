package com.yearhum.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yearhum.app.domain.model.UserSettings
import com.yearhum.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Holds the theme-related settings for the whole app; null until DataStore has loaded (avoids a theme flash). */
@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        settings: SettingsRepository,
    ) : ViewModel() {
        val settings: StateFlow<UserSettings?> =
            settings.settings
                .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }
