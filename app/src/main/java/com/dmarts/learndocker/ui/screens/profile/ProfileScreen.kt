package com.dmarts.learndocker.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.domain.model.UserProgress
import com.dmarts.learndocker.ui.components.XpBar
import com.dmarts.learndocker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = (context.applicationContext as LearnDockerApp).container
    var progress by remember { mutableStateOf(UserProgress()) }

    LaunchedEffect(Unit) {
        container.progressRepository.progressFlow.collect { progress = it }
    }

    val chaptersCompleted = container.chapterRegistry.all().count { ch ->
        val lastIdx = ch.levels.size - 1
        (progress.chapterProgress[ch.id] ?: -1) >= lastIdx
    }

    Scaffold(
        containerColor = NeoBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeoTextSecondary)
                    }
                },
                title = { Text("PROFILE", color = NeoCyan, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeoSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(progress.userName.ifBlank { "CIPHER" }, color = NeoCyan, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        when {
                            progress.totalXp >= 5000 -> "NEXUS Master"
                            progress.totalXp >= 2000 -> "Senior Engineer"
                            progress.totalXp >= 1000 -> "Engineer"
                            progress.totalXp >= 500  -> "Junior Engineer"
                            else -> "Recruit"
                        },
                        color = NeoTextSecondary, fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    XpBar(progress.totalXp, modifier = Modifier.fillMaxWidth())
                }
            }

            // Stats grid
            Text("STATISTICS", color = NeoTextSecondary, fontSize = 11.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.Build, "${progress.commandsExecuted}", "Commands", NeoCyan, Modifier.weight(1f))
                StatCard(Icons.Default.CheckCircle, "${progress.containersCreated}", "Containers", NeoGreen, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.Favorite, "${progress.currentStreak}d", "Streak", NeoRed, Modifier.weight(1f))
                StatCard(Icons.Default.Flag, "$chaptersCompleted/13", "Chapters", NeoAmber, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Icons.Default.EmojiEvents, "${progress.unlockedAchievementIds.size}", "Achievements", XpGold, Modifier.weight(1f))
                StatCard(Icons.Default.Code, "${progress.sandboxCommandsExecuted}", "Sandbox", NeoPurple, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String, label: String, color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeoSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Column {
                Text(value, color = NeoTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(label, color = NeoTextSecondary, fontSize = 10.sp)
            }
        }
    }
}
