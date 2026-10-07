package com.yearhum.app.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.common.asUiState
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        private val favorites: FavoritesRepository,
    ) : ViewModel() {
        val state: StateFlow<UiState<List<CapsuleItem>>> =
            favorites
                .observeFavorites()
                .asUiState()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

        /** Toggling an item that is currently listed removes it. */
        fun remove(itemId: Long) {
            viewModelScope.launch { favorites.toggle(itemId) }
        }
    }
