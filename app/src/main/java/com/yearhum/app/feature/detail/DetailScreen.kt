package com.yearhum.app.feature.detail

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yearhum.app.R
import com.yearhum.app.core.designsystem.component.ArtworkImage
import com.yearhum.app.core.designsystem.component.EmptyState
import com.yearhum.app.core.designsystem.component.LoadingState
import com.yearhum.app.core.designsystem.component.isChartRanked
import com.yearhum.app.core.designsystem.component.labelRes
import com.yearhum.app.core.designsystem.component.sharedArtwork
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.model.Category
import kotlinx.coroutines.launch

/** Links only: we never embed or stream audio. */
object StreamingLinks {
    private fun query(item: CapsuleItem) = Uri.encode(listOfNotNull(item.subtitle, item.title).joinToString(" "))

    fun youtube(item: CapsuleItem) = "https://www.youtube.com/results?search_query=${query(item)}"

    fun youtubeMusic(item: CapsuleItem) = "https://music.youtube.com/search?q=${query(item)}"

    fun spotify(item: CapsuleItem) = "https://open.spotify.com/search/${query(item)}"

    fun trailer(item: CapsuleItem) = "https://www.youtube.com/results?search_query=${Uri.encode("${item.title} ${item.year} trailer")}"

    fun wikipedia(item: CapsuleItem) = "https://en.wikipedia.org/w/index.php?search=${Uri.encode(item.title)}"
}

@Composable
fun DetailRoute(itemId: Long, onBack: () -> Unit) {
    val viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { it.create(itemId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    DetailScreen(state, onBack, viewModel::toggleFavorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(state: DetailUiState, onBack: () -> Unit, onToggleFavorite: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when (state) {
                DetailUiState.Loading -> LoadingState()
                DetailUiState.NotFound -> EmptyState(stringResource(R.string.error_generic))
                is DetailUiState.Content -> DetailContent(state, onToggleFavorite, snackbar)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    state: DetailUiState.Content,
    onToggleFavorite: () -> Unit,
    snackbar: SnackbarHostState,
) {
    val item = state.item
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val failedMessage = stringResource(R.string.link_failed)
    val open: (String) -> Unit = { url ->
        runCatching { uriHandler.openUri(url) }.onFailure {
            scope.launch {
                snackbar.showSnackbar(
                    failedMessage,
                )
            }
        }
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ArtworkImage(
            item.artworkUrl(500),
            item.title,
            Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .sharedArtwork(item.id),
        )
        Text(item.title, style = MaterialTheme.typography.headlineMedium)
        item.subtitle?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        if (item.category.isChartRanked) {
            Text(stringResource(R.string.detail_year_end_rank, item.rank, item.year))
            when (val extra = state.enrichment) {
                EnrichmentState.Loading -> Text(stringResource(R.string.detail_loading_details))

                EnrichmentState.Unavailable -> Text(stringResource(R.string.detail_details_unavailable))

                is EnrichmentState.Loaded -> extra.info.firstReleaseDate?.let {
                    Text(stringResource(R.string.detail_first_released, it))
                }
            }
        } else {
            Text(
                stringResource(
                    R.string.detail_category_in_year,
                    stringResource(item.category.labelRes()),
                    item.year,
                ),
            )
        }
        FilterChip(
            selected = state.isFavorite,
            onClick = onToggleFavorite,
            label = {
                Text(
                    stringResource(if (state.isFavorite) R.string.favorite_remove else R.string.favorite_add),
                )
            },
        )
        Text(stringResource(R.string.detail_listen_on), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (item.category.isChartRanked) {
                AssistChip(
                    onClick = { open(StreamingLinks.youtube(item)) },
                    label = { Text(stringResource(R.string.link_youtube)) },
                )
                AssistChip(
                    onClick = { open(StreamingLinks.youtubeMusic(item)) },
                    label = { Text(stringResource(R.string.link_youtube_music)) },
                )
                AssistChip(
                    onClick = { open(StreamingLinks.spotify(item)) },
                    label = { Text(stringResource(R.string.link_spotify)) },
                )
            } else {
                if (item.category in TRAILER_CATEGORIES) {
                    AssistChip(
                        onClick = { open(StreamingLinks.trailer(item)) },
                        label = { Text(stringResource(R.string.link_trailer)) },
                    )
                }
                AssistChip(
                    onClick = { open(StreamingLinks.wikipedia(item)) },
                    label = { Text(stringResource(R.string.link_wikipedia)) },
                )
            }
        }
    }
}

private val TRAILER_CATEGORIES = setOf(Category.GAME, Category.MOVIE, Category.TV)
