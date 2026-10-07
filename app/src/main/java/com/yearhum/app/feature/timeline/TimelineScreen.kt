package com.yearhum.app.feature.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yearhum.app.R
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.designsystem.component.ArtworkImage
import com.yearhum.app.core.designsystem.component.EmptyState
import com.yearhum.app.core.designsystem.component.ErrorState
import com.yearhum.app.core.designsystem.component.LoadingState
import com.yearhum.app.core.designsystem.component.isChartRanked
import com.yearhum.app.core.designsystem.component.sharedArtwork
import com.yearhum.app.core.designsystem.theme.DecadeTheme
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.model.Category
import com.yearhum.app.domain.model.YearCapsule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineRoute(
    startYear: Int,
    onBack: () -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val years by viewModel.years.collectAsStateWithLifecycle()
    Scaffold(
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
                .fillMaxSize()
        ) {
            when (val state = years) {
                UiState.Loading -> LoadingState()
                UiState.Error -> ErrorState(onRetry = null)
                is UiState.Content -> if (state.data.isEmpty()) {
                    EmptyState(stringResource(R.string.home_empty))
                } else {
                    YearPager(state.data, startYear, viewModel, onItemClick)
                }
            }
        }
    }
}

@Composable
private fun YearPager(
    years: List<Int>,
    startYear: Int,
    viewModel: TimelineViewModel,
    onItemClick: (Long) -> Unit
) {
    val initial = years.indexOf(startYear).takeIf { it >= 0 } ?: years.lastIndex
    val pagerState = rememberPagerState(initialPage = initial) { years.size }
    HorizontalPager(pagerState, Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
        val year = years[page]
        val flow = remember(year) { viewModel.capsule(year) }
        val state by flow.collectAsStateWithLifecycle(UiState.Loading)
        DecadeTheme(year) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                YearPage(year, state, onItemClick)
            }
        }
    }
}

@Composable
fun YearPage(
    year: Int,
    state: UiState<YearCapsule?>,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        UiState.Loading -> LoadingState(modifier)
        UiState.Error -> ErrorState(onRetry = null, modifier = modifier)
        is UiState.Content -> {
            val capsule = state.data
            if (capsule == null) {
                EmptyState(stringResource(R.string.year_not_available, year), modifier)
            } else {
                CapsuleContent(capsule, onItemClick, modifier)
            }
        }
    }
}

@Composable
private fun CapsuleContent(
    capsule: YearCapsule,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 16.dp)) {
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(capsule.year.toString(), style = MaterialTheme.typography.displayLarge)
                Text(capsule.headline, style = MaterialTheme.typography.titleMedium)
            }
        }
        // Music always shows (with an empty hint); the curated sections only appear when the year has data.
        section(
            R.string.section_songs,
            capsule.items(Category.SONG),
            onItemClick,
            showWhenEmpty = true
        )
        section(
            R.string.section_albums,
            capsule.items(Category.ALBUM),
            onItemClick,
            showWhenEmpty = true
        )
        section(R.string.section_games, capsule.items(Category.GAME), onItemClick)
        section(R.string.section_movies, capsule.items(Category.MOVIE), onItemClick)
        section(R.string.section_tv, capsule.items(Category.TV), onItemClick)
        section(R.string.section_culture, capsule.items(Category.EVENT), onItemClick)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    titleRes: Int,
    items: List<CapsuleItem>,
    onItemClick: (Long) -> Unit,
    showWhenEmpty: Boolean = false,
) {
    if (items.isEmpty() && !showWhenEmpty) return
    item(key = "section-$titleRes") {
        Column(Modifier.padding(top = 24.dp)) {
            Text(
                stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (items.isEmpty()) {
                Text(
                    stringResource(R.string.year_empty_section),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { ItemCard(it, onClick = { onItemClick(it.id) }) }
                }
            }
        }
    }
}

@Composable
fun ItemCard(item: CapsuleItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ranked = item.category.isChartRanked
    val description = when {
        ranked && item.subtitle != null ->
            stringResource(R.string.item_rank_title_artist, item.rank, item.title, item.subtitle)

        ranked -> stringResource(R.string.item_rank_title, item.rank, item.title)
        item.subtitle != null -> stringResource(
            R.string.item_title_subtitle,
            item.title,
            item.subtitle
        )

        else -> item.title
    }
    Column(
        modifier
            .width(150.dp)
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Box {
            ArtworkImage(
                item.artworkUrl(250),
                item.title,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .sharedArtwork(item.id),
            )
            // Rank badges only make sense for real charts; games/movies/TV/culture are editorial picks.
            if (ranked) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(6.dp),
                ) {
                    Text(
                        stringResource(R.string.item_rank, item.rank),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Text(
            item.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 6.dp)
                .clearAndSetSemantics {},
        )
        item.subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clearAndSetSemantics {},
            )
        }
    }
}
