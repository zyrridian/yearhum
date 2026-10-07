package com.yearhum.app.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.yearhum.app.core.designsystem.component.LocalSharedTransitionScope
import com.yearhum.app.feature.about.AboutScreen
import com.yearhum.app.feature.detail.DetailRoute
import com.yearhum.app.feature.favorites.FavoritesRoute
import com.yearhum.app.feature.home.HomeRoute
import com.yearhum.app.feature.lifetimeline.LifeTimelineRoute
import com.yearhum.app.feature.settings.SettingsRoute
import com.yearhum.app.feature.timeline.TimelineRoute
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data class Timeline(val startYear: Int) : NavKey

@Serializable
data class Detail(val itemId: Long) : NavKey

@Serializable
data object About : NavKey

@Serializable
data object LifeTimeline : NavKey

@Serializable
data object Favorites : NavKey

@Serializable
data object Settings : NavKey

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavDisplay() {
    val backStack = rememberNavBackStack(Home)
    val pop: () -> Unit = { backStack.removeLastOrNull() }
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = pop,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<Home> {
                        HomeRoute(
                            onGoToYear = { backStack.add(Timeline(it)) },
                            onOpenAbout = { backStack.add(About) },
                            onOpenLifeTimeline = { backStack.add(LifeTimeline) },
                            onOpenFavorites = { backStack.add(Favorites) },
                            onOpenSettings = { backStack.add(Settings) },
                        )
                    }
                    entry<Timeline> { key ->
                        TimelineRoute(
                            key.startYear,
                            onBack = pop,
                            onItemClick = { backStack.add(Detail(it)) },
                        )
                    }
                    entry<Detail> { key -> DetailRoute(key.itemId, onBack = pop) }
                    entry<About> { AboutScreen(onBack = pop) }
                    entry<LifeTimeline> {
                        LifeTimelineRoute(
                            onBack = pop,
                            onOpenYear = { backStack.add(Timeline(it)) },
                            onItemClick = { backStack.add(Detail(it)) },
                        )
                    }
                    entry<Favorites> {
                        FavoritesRoute(
                            onBack = pop,
                            onItemClick = { backStack.add(Detail(it)) },
                        )
                    }
                    entry<Settings> { SettingsRoute(onBack = pop) }
                },
            )
        }
    }
}
