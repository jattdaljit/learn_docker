package com.dmarts.learndocker.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.domain.model.AchievementDef
import com.dmarts.learndocker.domain.model.UserProgress
import com.dmarts.learndocker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as LearnDockerApp).container
    val allAchievements = container.achievementRegistry.all()
    var progress by remember { mutableStateOf(UserProgress()) }

    LaunchedEffect(Unit) {
        container.progressRepository.progressFlow.collect { progress = it }
    }

    val unlocked = progress.unlockedAchievementIds
    val unlockedCount = allAchievements.count { it.id in unlocked }

    Scaffold(
        containerColor = NeoBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeoTextSecondary)
                    }
                },
                title = {
                    Column {
                        Text("ACHIEVEMENTS", color = XpGold, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text("$unlockedCount / ${allAchievements.size} unlocked", color = NeoTextSecondary, fontSize = 10.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allAchievements) { achievement ->
                AchievementCard(achievement, achievement.id in unlocked)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: AchievementDef, isUnlocked: Boolean) {
    val borderColor = if (isUnlocked) XpGold.copy(alpha = 0.5f) else NeoBorder
    val alpha = if (isUnlocked) 1f else 0.4f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) NeoSurface else NeoBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) XpGold.copy(alpha = 0.15f) else NeoSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    null,
                    tint = if (isUnlocked) XpGold else NeoTextMuted,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                achievement.title,
                color = if (isUnlocked) NeoTextPrimary else NeoTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                achievement.description,
                color = NeoTextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "+${achievement.xpReward} XP",
                color = if (isUnlocked) XpGold else NeoTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
