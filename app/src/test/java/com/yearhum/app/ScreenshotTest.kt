package com.yearhum.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.yearhum.app.core.common.UiState
import com.yearhum.app.core.designsystem.theme.YearhumTheme
import com.yearhum.app.domain.model.CapsuleItem
import com.yearhum.app.domain.model.Category
import com.yearhum.app.domain.model.ReleaseGroupInfo
import com.yearhum.app.domain.model.YearCapsule
import com.yearhum.app.feature.detail.DetailContent
import com.yearhum.app.feature.detail.DetailUiState
import com.yearhum.app.feature.detail.EnrichmentState
import com.yearhum.app.feature.home.HomeScreen
import com.yearhum.app.feature.timeline.YearPage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-480dpi")
class ScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val outputDir = File("../store/screenshots").apply { mkdirs() }

    @Test
    fun captureHomeScreen() {
        composeTestRule.setContent {
            YearhumTheme {
                HomeScreen(
                    state = UiState.Content((1990..2026).toList()),
                    onGoToYear = {},
                    onOpenAbout = {},
                    onOpenFavorites = {},
                    onOpenSettings = {},
                    onOpenLifeTimeline = {},
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage(File(outputDir, "1_home.png").path)
    }

    @Test
    fun captureTimelineScreen() {
        val sampleItems = listOf(
            CapsuleItem(1, 2007, Category.SONG, 1, "Umbrella", "Rihanna ft. Jay-Z", null, null, null, null),
            CapsuleItem(2, 2007, Category.SONG, 2, "Irreplaceable", "Beyoncé", null, null, null, null),
            CapsuleItem(3, 2007, Category.ALBUM, 1, "As I Am", "Alicia Keys", null, null, null, null),
            CapsuleItem(4, 2007, Category.ALBUM, 2, "Noel", "Josh Groban", null, null, null, null),
        )
        val sampleCapsule = YearCapsule(2007, "Year-end No. 1: \"Umbrella\" by Rihanna", null, sampleItems)

        composeTestRule.setContent {
            YearhumTheme {
                YearPage(
                    year = 2007,
                    state = UiState.Content(sampleCapsule),
                    onItemClick = {},
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage(File(outputDir, "2_timeline.png").path)
    }

    @Test
    fun captureDetailScreen() {
        val sampleItem = CapsuleItem(1, 2007, Category.SONG, 1, "Umbrella", "Rihanna ft. Jay-Z", null, null, null, null)
        val detailState = DetailUiState.Content(
            item = sampleItem,
            enrichment = EnrichmentState.Loaded(ReleaseGroupInfo("sample-mbid", "Umbrella", "Rihanna", "2007-03-29")),
            isFavorite = true,
        )

        composeTestRule.setContent {
            YearhumTheme {
                DetailContent(
                    state = detailState,
                    onToggleFavorite = {},
                    snackbar = androidx.compose.material3.SnackbarHostState(),
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage(File(outputDir, "3_detail.png").path)
    }
}
