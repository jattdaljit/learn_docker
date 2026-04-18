package com.dmarts.learndocker.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }

    val logoAlpha by animateFloatAsState(
        if (phase >= 1) 1f else 0f,
        animationSpec = tween(800), label = "logo"
    )
    val logoScale by animateFloatAsState(
        if (phase >= 1) 1f else 0.6f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val subtitleAlpha by animateFloatAsState(
        if (phase >= 2) 1f else 0f,
        animationSpec = tween(600), label = "subtitle"
    )
    val storyAlpha by animateFloatAsState(
        if (phase >= 3) 1f else 0f,
        animationSpec = tween(600), label = "story"
    )

    LaunchedEffect(Unit) {
        delay(300); phase = 1
        delay(600); phase = 2
        delay(800); phase = 3
        delay(1200); onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(NeoBackground, NeoSurface, NeoBackground))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .alpha(logoAlpha)
                    .scale(logoScale)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "◈",
                        color = NeoCyan,
                        fontSize = 64.sp
                    )
                    Text(
                        "DOCKER\nLEARN",
                        color = NeoTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp,
                        letterSpacing = 4.sp
                    )
                }
            }

            Text(
                "StackForge Inc. · DevOps Training",
                color = NeoCyan,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(subtitleAlpha)
            )

            Text(
                "LOADING SYSTEMS...",
                color = NeoGreen,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.alpha(storyAlpha)
            )
        }
    }
}
