package com.example.yearhum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.yearhum.core.designsystem.theme.LocalDecadeThemes
import com.example.yearhum.core.designsystem.theme.YearhumTheme
import com.example.yearhum.domain.model.ThemeMode
import com.example.yearhum.navigation.AppNavDisplay
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            // Wait for stored preferences so the app never flashes the wrong theme.
            settings?.let { current ->
                val dark = when (current.themeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }
                YearhumTheme(darkTheme = dark, dynamicColor = current.dynamicColor) {
                    CompositionLocalProvider(LocalDecadeThemes provides current.decadeThemes) {
                        AppNavDisplay()
                    }
                }
            }
        }
    }
}
