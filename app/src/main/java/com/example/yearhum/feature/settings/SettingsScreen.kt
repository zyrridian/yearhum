package com.example.yearhum.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.yearhum.R
import com.example.yearhum.core.designsystem.component.BirthYearDialog
import com.example.yearhum.domain.model.ThemeMode
import com.example.yearhum.domain.model.UserSettings
import java.util.Locale

private val COUNTRY_CODES = listOf("US", "GB", "JP", "ID", "KR", "DE", "FR", "BR", "AU", "CA")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val clearedMessage = stringResource(R.string.settings_cache_cleared)
    LaunchedEffect(viewModel) {
        viewModel.cacheClearedEvents.collect { snackbar.showSnackbar(clearedMessage) }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } },
            )
        },
    ) { padding ->
        SettingsContent(
            settings = settings,
            onSetBirthYear = viewModel::setBirthYear,
            onSetCountry = viewModel::setCountry,
            onSetThemeMode = viewModel::setThemeMode,
            onSetDynamicColor = viewModel::setDynamicColor,
            onSetDecadeThemes = viewModel::setDecadeThemes,
            onClearCache = viewModel::clearCache,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsContent(
    settings: UserSettings,
    onSetBirthYear: (Int?) -> Unit,
    onSetCountry: (String?) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetDecadeThemes: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var editingBirthYear by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionTitle(R.string.settings_birth_year)
        Text(
            settings.birthYear?.toString() ?: stringResource(R.string.settings_birth_year_not_set),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                editingBirthYear = true
            }) { Text(stringResource(R.string.settings_edit)) }
            if (settings.birthYear != null) {
                TextButton(onClick = { onSetBirthYear(null) }) { Text(stringResource(R.string.settings_clear)) }
            }
        }

        SectionTitle(R.string.settings_country)
        Text(
            stringResource(R.string.settings_country_hint),
            style = MaterialTheme.typography.bodySmall
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = settings.preferredCountry == null,
                onClick = { onSetCountry(null) },
                label = { Text(stringResource(R.string.settings_country_none)) },
            )
            COUNTRY_CODES.forEach { code ->
                FilterChip(
                    selected = settings.preferredCountry == code,
                    onClick = { onSetCountry(code) },
                    label = { Text(Locale("", code).getDisplayCountry(Locale.getDefault())) },
                )
            }
        }

        SectionTitle(R.string.settings_theme)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.themeMode == mode,
                    onClick = { onSetThemeMode(mode) },
                    label = { Text(stringResource(mode.labelRes())) },
                )
            }
        }
        SwitchRow(R.string.settings_dynamic_color, settings.dynamicColor, onSetDynamicColor)
        SwitchRow(R.string.settings_decade_themes, settings.decadeThemes, onSetDecadeThemes)

        SectionTitle(R.string.settings_data)
        Text(
            stringResource(R.string.settings_cache_hint),
            style = MaterialTheme.typography.bodySmall
        )
        Button(onClick = onClearCache) { Text(stringResource(R.string.settings_clear_cache)) }
    }
    if (editingBirthYear) {
        BirthYearDialog(
            initialYear = settings.birthYear,
            onConfirm = {
                onSetBirthYear(it)
                editingBirthYear = false
            },
            onDismiss = { editingBirthYear = false },
            dismissLabel = stringResource(R.string.cancel),
        )
    }
}

private fun ThemeMode.labelRes() = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

@Composable
private fun SectionTitle(res: Int) {
    Text(
        stringResource(res),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SwitchRow(labelRes: Int, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(labelRes),
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
