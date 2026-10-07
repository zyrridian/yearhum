package com.example.yearhum.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yearhum.core.common.UiState
import com.example.yearhum.core.common.asUiState
import com.example.yearhum.domain.model.UserSettings
import com.example.yearhum.domain.repository.SettingsRepository
import com.example.yearhum.domain.repository.YearRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: YearRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val years: StateFlow<UiState<List<Int>>> = repository.observeYears()
        .asUiState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Null until DataStore has loaded, so the onboarding prompt never flashes for returning users. */
    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun saveBirthYear(year: Int) {
        viewModelScope.launch { settingsRepository.setBirthYear(year) }
    }

    fun skipOnboarding() {
        viewModelScope.launch { settingsRepository.completeOnboarding() }
    }
}
