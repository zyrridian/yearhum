package com.yearhum.app.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.common.asUiState
import com.yearhum.app.domain.model.YearCapsule
import com.yearhum.app.domain.repository.YearRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel
    @Inject
    constructor(
        private val repository: YearRepository,
    ) : ViewModel() {
        val years: StateFlow<UiState<List<Int>>> =
            repository
                .observeYears()
                .asUiState()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

        /** Content is null when the year is not in the shipped dataset. */
        fun capsule(year: Int): Flow<UiState<YearCapsule?>> = repository.observeCapsule(year).asUiState()
    }
