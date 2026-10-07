package com.yearhum.app.feature.lifetimeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yearhum.app.core.common.TimeProvider
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.common.asUiState
import com.yearhum.app.domain.model.LifeMilestone
import com.yearhum.app.domain.model.YearCapsule
import com.yearhum.app.domain.repository.SettingsRepository
import com.yearhum.app.domain.repository.YearRepository
import com.yearhum.app.domain.usecase.BuildLifeTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

sealed interface LifeTimelineUiState {
    data object Loading : LifeTimelineUiState

    data object NeedsBirthYear : LifeTimelineUiState

    data class Content(
        val birthYear: Int,
        val milestones: List<LifeMilestone>,
    ) : LifeTimelineUiState
}

@HiltViewModel
class LifeTimelineViewModel
@Inject
constructor(
    private val repository: YearRepository,
    private val settings: SettingsRepository,
    buildTimeline: BuildLifeTimelineUseCase,
    time: TimeProvider,
) : ViewModel() {
    private val currentYear =
        Instant.ofEpochMilli(time.nowMillis()).atZone(ZoneId.systemDefault()).year

    val state: StateFlow<LifeTimelineUiState> =
        combine(
            settings.settings.map { it.birthYear }.distinctUntilChanged(),
            repository.observeYears(),
        ) { birthYear, years ->
            if (birthYear == null) {
                LifeTimelineUiState.NeedsBirthYear
            } else {
                LifeTimelineUiState.Content(birthYear, buildTimeline(birthYear, years, currentYear))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LifeTimelineUiState.Loading)

    fun capsule(year: Int): Flow<UiState<YearCapsule?>> = repository.observeCapsule(year).asUiState()

    fun saveBirthYear(year: Int) {
        viewModelScope.launch { settings.setBirthYear(year) }
    }
}
