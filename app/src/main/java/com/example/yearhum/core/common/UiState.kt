package com.example.yearhum.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>

    data class Content<T>(val data: T) : UiState<T>

    data object Error : UiState<Nothing>
}

fun <T> Flow<T>.asUiState(): Flow<UiState<T>> = map<T, UiState<T>> { UiState.Content(it) }
    .onStart { emit(UiState.Loading) }
    .catch { emit(UiState.Error) }
