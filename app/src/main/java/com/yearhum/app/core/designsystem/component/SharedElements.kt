package com.yearhum.app.core.designsystem.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/** Set by AppNavDisplay; null in previews/tests, where [sharedArtwork] is a no-op. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** Shares an item's artwork between the timeline card and the detail screen. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedArtwork(itemId: Long): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animatedScope = LocalNavAnimatedContentScope.current
    return with(shared) {
        this@sharedArtwork.sharedElement(
            sharedContentState = rememberSharedContentState(key = "artwork-$itemId"),
            animatedVisibilityScope = animatedScope,
        )
    }
}
