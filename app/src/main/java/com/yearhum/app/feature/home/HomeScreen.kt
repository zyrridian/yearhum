package com.yearhum.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.yearhum.app.core.designsystem.component.BirthYearDialog
import com.yearhum.app.core.designsystem.component.EmptyState
import com.yearhum.app.core.designsystem.component.ErrorState
import com.yearhum.app.core.designsystem.component.LoadingState

@Composable
fun HomeRoute(
    onGoToYear: (Int) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLifeTimeline: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.years.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onGoToYear = onGoToYear,
        onOpenAbout = onOpenAbout,
        onOpenLifeTimeline = onOpenLifeTimeline,
        onOpenFavorites = onOpenFavorites,
        onOpenSettings = onOpenSettings,
        showOnboarding = settings?.let { !it.onboardingDone } ?: false,
        onSaveBirthYear = viewModel::saveBirthYear,
        onSkipOnboarding = viewModel::skipOnboarding,
    )
}

@Composable
fun HomeScreen(
    state: UiState<List<Int>>,
    onGoToYear: (Int) -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLifeTimeline: () -> Unit = {},
    onOpenFavorites: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    showOnboarding: Boolean = false,
    onSaveBirthYear: (Int) -> Unit = {},
    onSkipOnboarding: () -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Row(Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onOpenFavorites) { Text(stringResource(R.string.favorites_title)) }
            TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.settings_title)) }
            TextButton(onClick = onOpenAbout) { Text(stringResource(R.string.about)) }
        }
        when (state) {
            UiState.Loading -> LoadingState()

            UiState.Error -> ErrorState(onRetry = null)

            is UiState.Content ->
                if (state.data.isEmpty()) {
                    EmptyState(stringResource(R.string.home_empty))
                } else {
                    YearPicker(state.data, onGoToYear, onOpenLifeTimeline)
                }
        }
    }
    if (showOnboarding) {
        BirthYearDialog(
            initialYear = null,
            onConfirm = onSaveBirthYear,
            onDismiss = onSkipOnboarding,
        )
    }
}

@Composable
fun YearPicker(
    years: List<Int>,
    onGoToYear: (Int) -> Unit,
    onOpenLifeTimeline: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val min = years.first()
    val max = years.last()
    var year by rememberSaveable {
        mutableIntStateOf(years.firstOrNull { it == DEFAULT_YEAR } ?: max)
    }
    year = year.coerceIn(min, max)
    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.home_take_me_to), style = MaterialTheme.typography.titleLarge)
        val yearLabel = stringResource(R.string.home_year_value, year)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilledTonalIconButton(
                onClick = { year = (year - 1).coerceAtLeast(min) },
                enabled = year > min,
            ) {
                Text("−", modifier = Modifier.semantics { contentDescription = "" })
            }
            Text(
                year.toString(),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.semantics { contentDescription = yearLabel },
            )
            FilledTonalIconButton(
                onClick = { year = (year + 1).coerceAtMost(max) },
                enabled = year < max,
            ) {
                Text("+")
            }
        }
        Button(onClick = { onGoToYear(year) }, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.home_go))
        }
        OutlinedButton(
            onClick = { onGoToYear(years.random()) },
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text(stringResource(R.string.home_surprise))
        }
        TextButton(onClick = onOpenLifeTimeline, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.home_life_in_music))
        }
    }
}

private const val DEFAULT_YEAR = 2007
