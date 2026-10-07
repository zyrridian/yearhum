package com.yearhum.app.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserSettings(
    val birthYear: Int? = null,
    /** True once the first-launch birth-year prompt was answered or skipped. */
    val onboardingDone: Boolean = false,
    /** ISO 3166-1 alpha-2 code; stored now, used by the upcoming country lens. */
    val preferredCountry: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val decadeThemes: Boolean = true,
)

/** A year in the user's life worth showing: "You were [age]". */
data class LifeMilestone(
    val age: Int,
    val year: Int,
)
