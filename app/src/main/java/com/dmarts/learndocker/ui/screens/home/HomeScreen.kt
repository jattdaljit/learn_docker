package com.dmarts.learndocker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.domain.model.Chapter
import com.dmarts.learndocker.ui.components.ChapterStatusBadge
import com.dmarts.learndocker.ui.components.XpBar
import com.dmarts.learndocker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onChapterClick: (String) -> Unit,
    onSandboxClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCommandsClick: () -> Unit = {},
    onDockerHubClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel((context.applicationContext as LearnDockerApp).container) as T
        }
    })
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NeoBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Docker Learn", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = NeoCyan)
                        Text("Learn Docker by doing", fontSize = 12.sp, color = NeoTextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = onAchievementsClick) {
                        Icon(Icons.Default.EmojiEvents, "Achievements", tint = XpGold)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, "Profile", tint = NeoTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Player status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeoSurface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CIPHER", color = NeoCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(state.playerTitle, color = NeoTextSecondary, fontSize = 11.sp)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatChip(Icons.Default.Favorite, "${state.progress.currentStreak}d", NeoRed)
                            StatChip(Icons.Default.Star, "${state.progress.totalXp} XP", XpGold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    XpBar(state.progress.totalXp, modifier = Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick actions — 2×2 grid, learning front and centre
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Primary: Continue learning — full width
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            state.chapters.firstOrNull { ch ->
                                (state.progress.chapterProgress[ch.id] ?: -1) < ch.levels.size - 1
                            }?.let { onChapterClick(it.id) }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoCyan.copy(alpha = 0.12f)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(NeoCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.PlayArrow, null, tint = NeoCyan, modifier = Modifier.size(24.dp)) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Continue Learning", color = NeoCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(state.currentLevelLabel, color = NeoTextSecondary, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.KeyboardArrowRight, null, tint = NeoCyan, modifier = Modifier.size(20.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionButton(
                        icon = Icons.Default.Terminal,
                        label = "Sandbox",
                        color = NeoGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onSandboxClick
                    )
                    QuickActionButton(
                        icon = Icons.Default.Search,
                        label = "Docker Hub",
                        color = Color(0xFF0EA5E9),
                        modifier = Modifier.weight(1f),
                        onClick = onDockerHubClick
                    )
                    QuickActionButton(
                        icon = Icons.Default.Book,
                        label = "Reference",
                        color = NeoPurple,
                        modifier = Modifier.weight(1f),
                        onClick = onCommandsClick
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Chapters",
                color = NeoTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.chapters) { chapter ->
                    val unlocked = vm.isChapterUnlocked(chapter, state.progress)
                    val completionPct = vm.getChapterCompletionPercent(chapter, state.progress)
                    val completed = completionPct >= 1f
                    ChapterCard(
                        chapter = chapter,
                        unlocked = unlocked,
                        completed = completed,
                        completionPct = completionPct,
                        onClick = { if (unlocked) onChapterClick(chapter.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Text(label, color = NeoTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun ChapterCard(
    chapter: Chapter,
    unlocked: Boolean,
    completed: Boolean,
    completionPct: Float,
    onClick: () -> Unit
) {
    val borderColor = when {
        completed -> NeoGreen.copy(alpha = 0.5f)
        unlocked  -> NeoCyan.copy(alpha = 0.3f)
        else      -> NeoBorder
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unlocked) NeoSurface else NeoBackground
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "Ch.${chapter.number}",
                    color = if (unlocked) NeoCyan else NeoTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                ChapterStatusBadge(unlocked, completed)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                chapter.title,
                color = if (unlocked) NeoTextPrimary else NeoTextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                chapter.subtitle,
                color = NeoTextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )
            Spacer(Modifier.weight(1f))
            if (unlocked) {
                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NeoSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(completionPct)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (completed) NeoGreen else NeoCyan)
                    )
                }
            }
        }
    }
}
