package com.yearhum.app.feature.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yearhum.app.R
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.designsystem.component.ArtworkImage
import com.yearhum.app.core.designsystem.component.EmptyState
import com.yearhum.app.core.designsystem.component.ErrorState
import com.yearhum.app.core.designsystem.component.LoadingState
import com.yearhum.app.core.designsystem.component.labelRes
import com.yearhum.app.domain.model.CapsuleItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesRoute(
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            FavoritesContent(state, onItemClick, viewModel::remove)
        }
    }
}

@Composable
fun FavoritesContent(
    state: UiState<List<CapsuleItem>>,
    onItemClick: (Long) -> Unit,
    onRemove: (Long) -> Unit
) {
    when (state) {
        UiState.Loading -> LoadingState()
        UiState.Error -> ErrorState(onRetry = null)
        is UiState.Content -> if (state.data.isEmpty()) {
            EmptyState(stringResource(R.string.favorites_empty))
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.data, key = { it.id }) { item ->
                    FavoriteRow(
                        item,
                        onClick = { onItemClick(item.id) },
                        onRemove = { onRemove(item.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(item: CapsuleItem, onClick: () -> Unit, onRemove: () -> Unit) {
    val category = stringResource(item.category.labelRes())
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ArtworkImage(item.artworkUrl(250), item.title, Modifier.size(56.dp))
        Column(
            Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) { contentDescription = "" }) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            item.subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            Text("$category · ${item.year}", style = MaterialTheme.typography.labelMedium)
        }
        TextButton(onClick = onRemove) { Text(stringResource(R.string.favorites_remove)) }
    }
}
