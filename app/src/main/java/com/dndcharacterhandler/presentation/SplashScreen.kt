package com.dndcharacterhandler.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dndcharacterhandler.R
import com.dndcharacterhandler.presentation.theme.DnDTheme
import kotlinx.coroutines.delay

/** Required CC-BY-4.0 attribution for the SRD 5.2 content bundled in the catalog. */
private const val SRD_ATTRIBUTION =
    "This work includes material from the SRD 5.2 by Wizards of the Coast LLC, licensed under CC-BY-4.0"

/**
 * Launch splash showing the studio logo centered with the SRD attribution pinned to the bottom.
 * Calls [onTimeout] after a short delay to hand off to the app.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }
    SplashContent()
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120E18)) // colors.materialTheme.background
    ) {
        Image(
            painter = painterResource(R.drawable.studio_logo),
            contentDescription = "Mind Eaters Tea Ceremony",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.72f)
        )
        Text(
            text = SRD_ATTRIBUTION,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFD2CAC2), // colors.overview.textMuted
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        )
    }
}

@Preview(name = "Splash", showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    DnDTheme {
        SplashContent()
    }
}
