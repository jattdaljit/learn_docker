package com.dmarts.learndocker.ui.screens.levelcomplete

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.first
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.ads.InterstitialAdManager
import com.dmarts.learndocker.ui.theme.*

@Composable
fun LevelCompleteScreen(
    chapterId: String,
    levelIndex: Int,
    xpEarned: Int,
    onNextLevel: () -> Unit,
    onChapterMap: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val container = (context.applicationContext as LearnDockerApp).container
    val level = container.chapterRegistry.byId(chapterId)?.levels?.getOrNull(levelIndex)
    val chapter = container.chapterRegistry.byId(chapterId)
    val hasNextLevel = (levelIndex + 1) < (chapter?.levels?.size ?: 0)
    val isLastLevelInChapter = !hasNextLevel

    // Show interstitial after every 2 completed chapters; preload for next trigger
    LaunchedEffect(Unit) {
        InterstitialAdManager.preload(context)
        if (isLastLevelInChapter && activity != null) {
            // Count how many chapters are now fully complete
            val progress = container.progressRepository.progressFlow.first()
            val allChapters = container.chapterRegistry.all()
            val completedChapterCount = allChapters.count { ch ->
                (progress.chapterProgress[ch.id] ?: -1) >= ch.levels.size - 1
            }
            if (completedChapterCount > 0 && completedChapterCount % 2 == 0) {
                InterstitialAdManager.showIfReady(activity)
            }
        }
    }

    val scale by animateFloatAsState(
        1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "icon"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(NeoGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = NeoGreen, modifier = Modifier.size(60.dp))
            }

            Text(
                "MISSION COMPLETE",
                color = NeoGreen,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            Text(
                level?.title ?: "",
                color = NeoTextPrimary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            // XP card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeoSurface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(Icons.Default.Star, "+$xpEarned", "XP EARNED", XpGold)
                    StatItem(Icons.Default.EmojiEvents, "${levelIndex + 1}", "LEVEL", NeoCyan)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Buttons
            if (hasNextLevel) {
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeoCyan, contentColor = NeoBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, null)
                    Spacer(Modifier.width(8.dp))
                    Text("NEXT LEVEL", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
            OutlinedButton(
                onClick = onChapterMap,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeoTextPrimary)
            ) {
                Icon(Icons.Default.Map, null)
                Spacer(Modifier.width(8.dp))
                Text("CHAPTER MAP")
            }
            TextButton(onClick = onHome) {
                Text("Home", color = NeoTextSecondary)
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String, label: String, color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = NeoTextSecondary, fontSize = 9.sp, letterSpacing = 1.sp)
    }
}
