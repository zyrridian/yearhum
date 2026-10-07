package com.yearhum.app.feature.lifetimeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yearhum.app.R
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.designsystem.component.BirthYearDialog
import com.yearhum.app.core.designsystem.component.EmptyState
import com.yearhum.app.core.designsystem.component.ErrorState
import com.yearhum.app.core.designsystem.component.LoadingState
import com.yearhum.app.core.designsystem.theme.DecadeTheme
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.model.Category
import com.yearhum.app.domain.model.LifeMilestone
import com.yearhum.app.feature.timeline.ItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTimelineRoute(
    onBack: () -> Unit,
    onOpenYear: (Int) -> Unit,
    onItemClick: (Long) -> Unit,
    viewModel: LifeTimelineViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editing by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.life_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } },
                actions = {
                    if (state is LifeTimelineUiState.Content) {
                        TextButton(onClick = {
                            editing = true
                        }) { Text(stringResource(R.string.life_change_birth_year)) }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val current = state) {
                LifeTimelineUiState.Loading -> LoadingState()
                LifeTimelineUiState.NeedsBirthYear -> NeedsBirthYear { editing = true }
                is LifeTimelineUiState.Content -> if (current.milestones.isEmpty()) {
                    EmptyState(stringResource(R.string.life_empty, current.birthYear))
                } else {
                    MilestoneList(current.milestones, viewModel, onOpenYear, onItemClick)
                }
            }
        }
    }
    if (editing) {
        val initial = (state as? LifeTimelineUiState.Content)?.birthYear
        BirthYearDialog(
            initialYear = initial,
            onConfirm = {
                viewModel.saveBirthYear(it)
                editing = false
            },
            onDismiss = { editing = false },
            dismissLabel = stringResource(R.string.cancel),
        )
    }
}

@Composable
private fun NeedsBirthYear(onEnter: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.life_needs_birth_year),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onEnter, modifier = Modifier.padding(top = 12.dp)) {
            Text(stringResource(R.string.life_enter_birth_year))
        }
    }
}

@Composable
private fun MilestoneList(
    milestones: List<LifeMilestone>,
    viewModel: LifeTimelineViewModel,
    onOpenYear: (Int) -> Unit,
    onItemClick: (Long) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
        items(milestones, key = { it.year }) { milestone ->
            val flow = remember(milestone.year) { viewModel.capsule(milestone.year) }
            val capsuleState by flow.collectAsStateWithLifecycle(UiState.Loading)
            DecadeTheme(milestone.year) {
                MilestoneCard(milestone, capsuleState, onOpenYear, onItemClick)
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: LifeMilestone,
    state: UiState<com.yearhum.app.domain.model.YearCapsule?>,
    onOpenYear: (Int) -> Unit,
    onItemClick: (Long) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(vertical = 16.dp)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(
                    if (milestone.age == 0) {
                        stringResource(R.string.life_born)
                    } else {
                        stringResource(R.string.life_you_were, milestone.age)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(milestone.year.toString(), style = MaterialTheme.typography.displayMedium)
            }
            when (state) {
                UiState.Loading -> LoadingState(Modifier.height(96.dp))
                UiState.Error -> ErrorState(onRetry = null, modifier = Modifier.height(96.dp))
                is UiState.Content -> state.data?.let { capsule ->
                    Text(
                        capsule.headline,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                    val highlights = highlights(capsule.items)
                    if (highlights.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(highlights, key = { it.id }) {
                                ItemCard(
                                    it,
                                    onClick = { onItemClick(it.id) })
                            }
                        }
                    }
                }
            }
            TextButton(
                onClick = { onOpenYear(milestone.year) },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(stringResource(R.string.life_open_year, milestone.year))
            }
        }
    }
}

/** Top three songs plus the leading entry of every other category: a taste of the year. */
internal fun highlights(items: List<CapsuleItem>): List<CapsuleItem> {
    val songs = items.filter { it.category == Category.SONG }.sortedBy { it.rank }.take(3)
    val others =
        listOf(Category.ALBUM, Category.MOVIE, Category.GAME, Category.TV).mapNotNull { category ->
            items.filter { it.category == category }.minByOrNull { it.rank }
        }
    return songs + others
}
