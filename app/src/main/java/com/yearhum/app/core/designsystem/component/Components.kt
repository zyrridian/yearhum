package com.yearhum.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.yearhum.app.R

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun ErrorState(onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.error_generic), style = MaterialTheme.typography.bodyLarge)
        if (onRetry != null) {
            Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

/** Deterministic color per title so missing artwork still looks intentional. */
fun placeholderColor(seed: String): Color {
    val hue = (seed.hashCode().toLong() and 0x7fffffff) % 360
    return Color.hsv(hue.toFloat(), 0.45f, 0.55f)
}

@Composable
fun Placeholder(title: String, modifier: Modifier = Modifier) {
    Box(modifier.background(placeholderColor(title)), contentAlignment = Alignment.Center) {
        Text(
            title.firstOrNull { it.isLetterOrDigit() }?.uppercase().orEmpty(),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
        )
    }
}

/** Cover Art Archive image with a generated placeholder while loading / on 404. */
@Composable
fun ArtworkImage(url: String?, title: String, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.artwork_description, title)
    val shape = RoundedCornerShape(12.dp)
    if (url == null) {
        Placeholder(
            title,
            modifier
                .clip(shape)
                .semantics { contentDescription = description },
        )
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = description,
            contentScale = ContentScale.Crop,
            loading = { Placeholder(title, Modifier.fillMaxSize()) },
            error = { Placeholder(title, Modifier.fillMaxSize()) },
            modifier = modifier.clip(shape),
        )
    }
}
