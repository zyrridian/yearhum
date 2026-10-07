package com.example.yearhum.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yearhum.core.common.AppResult
import com.example.yearhum.domain.model.CapsuleItem
import com.example.yearhum.domain.model.ReleaseGroupInfo
import com.example.yearhum.domain.repository.EnrichmentRepository
import com.example.yearhum.domain.repository.FavoritesRepository
import com.example.yearhum.domain.repository.YearRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Details fetched lazily from MusicBrainz; never blocks the screen. */
sealed interface EnrichmentState {
    data object Loading : EnrichmentState

    data class Loaded(val info: ReleaseGroupInfo) : EnrichmentState

    data object Unavailable : EnrichmentState
}

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data object NotFound : DetailUiState

    data class Content(
        val item: CapsuleItem,
        val enrichment: EnrichmentState,
        val isFavorite: Boolean,
    ) : DetailUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val itemId: Long,
    yearRepository: YearRepository,
    private val enrichment: EnrichmentRepository,
    private val favorites: FavoritesRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(itemId: Long): DetailViewModel
    }

    private val item = yearRepository.observeItem(itemId)

    private fun enrichmentFor(item: CapsuleItem): Flow<EnrichmentState> {
        val mbid = item.mbid ?: return flowOf(EnrichmentState.Unavailable)
        return enrichment.releaseGroup(mbid)
            .map {
                when (it) {
                    is AppResult.Success -> EnrichmentState.Loaded(it.value)
                    is AppResult.Failure -> EnrichmentState.Unavailable
                }
            }
            .onStart { emit(EnrichmentState.Loading) }
    }

    val state: StateFlow<DetailUiState> = item.flatMapLatest { current ->
        if (current == null) {
            flowOf(DetailUiState.NotFound)
        } else {
            combine(enrichmentFor(current), favorites.observeIsFavorite(itemId)) { extra, fav ->
                DetailUiState.Content(current, extra, fav)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    fun toggleFavorite() {
        viewModelScope.launch { favorites.toggle(itemId) }
    }
}
