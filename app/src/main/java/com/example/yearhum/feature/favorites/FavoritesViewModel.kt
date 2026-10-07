package com.example.yearhum.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yearhum.core.common.UiState
import com.example.yearhum.core.common.asUiState
import com.example.yearhum.domain.model.CapsuleItem
import com.example.yearhum.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val favorites: FavoritesRepository) : ViewModel() {
    val state: StateFlow<UiState<List<CapsuleItem>>> = favorites.observeFavorites()
        .asUiState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Toggling an item that is currently listed removes it. */
    fun remove(itemId: Long) {
        viewModelScope.launch { favorites.toggle(itemId) }
    }
}
