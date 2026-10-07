package com.yearhum.app.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Provided by MainActivity from the user's "decade themes" setting. */
val LocalDecadeThemes = compositionLocalOf { true }

/** Accent colors and backgrounds for one decade, layered over the base Material 3 scheme. */
data class DecadePalette(
    val decade: Int,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val lightBackground: Color,
    val darkBackground: Color,
)

private val Palettes = listOf(
    // 60s: warm retro orange + teal
    DecadePalette(
        1960,
        Color(0xFFE8742C),
        Color(0xFF2A9D8F),
        Color(0xFFF4C430),
        Color(0xFFFFF6E5),
        Color(0xFF2B1D0E)
    ),
    // 70s: burnt orange, olive, mustard
    DecadePalette(
        1970,
        Color(0xFFC15B1F),
        Color(0xFF7A8450),
        Color(0xFFD9A441),
        Color(0xFFF7EBD7),
        Color(0xFF2A2116)
    ),
    // 80s: neon magenta, cyan, yellow
    DecadePalette(
        1980,
        Color(0xFFFF2BD6),
        Color(0xFF00E5FF),
        Color(0xFFFFE600),
        Color(0xFFFFF0FB),
        Color(0xFF120B2E)
    ),
    // 90s: grunge flannel red, moss, concrete
    DecadePalette(
        1990,
        Color(0xFFB3412F),
        Color(0xFF6B7B5A),
        Color(0xFF9A8F7A),
        Color(0xFFE9E6DF),
        Color(0xFF1B1D1C)
    ),
    // 2000s: chrome blue and silver
    DecadePalette(
        2000,
        Color(0xFF2E7DD7),
        Color(0xFF8FA3B8),
        Color(0xFF00B5AD),
        Color(0xFFF0F5FA),
        Color(0xFF0F1721)
    ),
    // 2010s: flat coral and teal
    DecadePalette(
        2010,
        Color(0xFFFF6B6B),
        Color(0xFF1AA6B7),
        Color(0xFFFFB84D),
        Color(0xFFFFFBFA),
        Color(0xFF161616)
    ),
)

/** Null for years without a dedicated look (before 1960, and the 2020s which use the app's base theme). */
fun decadePaletteFor(year: Int): DecadePalette? =
    Palettes.firstOrNull { year in it.decade..it.decade + 9 }

private fun onColor(background: Color) =
    if (background.luminance() > 0.5f) Color.Black else Color.White

fun DecadePalette.applyTo(base: ColorScheme, dark: Boolean): ColorScheme {
    val background = if (dark) darkBackground else lightBackground
    return base.copy(
        primary = primary,
        onPrimary = onColor(primary),
        secondary = secondary,
        onSecondary = onColor(secondary),
        tertiary = tertiary,
        onTertiary = onColor(tertiary),
        background = background,
        onBackground = onColor(background),
        surface = background,
        onSurface = onColor(background),
    )
}

/** Re-themes [content] for the decade of [year]; no-op when disabled in settings or no palette exists. */
@Composable
fun DecadeTheme(year: Int, content: @Composable () -> Unit) {
    val palette = if (LocalDecadeThemes.current) decadePaletteFor(year) else null
    if (palette == null) {
        content()
    } else {
        val base = MaterialTheme.colorScheme
        val dark = base.background.luminance() < 0.5f
        MaterialTheme(
            colorScheme = palette.applyTo(base, dark),
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content,
        )
    }
}
