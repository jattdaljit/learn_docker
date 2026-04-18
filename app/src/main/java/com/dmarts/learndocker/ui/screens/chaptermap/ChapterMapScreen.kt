package com.dmarts.learndocker.ui.screens.chaptermap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.domain.model.Chapter
import com.dmarts.learndocker.domain.model.Level
import com.dmarts.learndocker.domain.model.UserProgress
import com.dmarts.learndocker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterMapScreen(
    chapterId: String,
    onLevelSelect: (chapterId: String, levelIndex: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as LearnDockerApp).container
    val chapter = container.chapterRegistry.byId(chapterId)

    var progress by remember { mutableStateOf(UserProgress()) }
    LaunchedEffect(Unit) {
        container.progressRepository.progressFlow.collect { progress = it }
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
                title = {
                    Column {
                        Text("Ch.${chapter?.number ?: ""} — ${chapter?.title ?: ""}", color = NeoCyan,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(chapter?.districtName ?: "", color = NeoTextSecondary, fontSize = 10.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        if (chapter == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chapter not found", color = NeoRed)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // Story intro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("MISSION BRIEFING", color = NeoCyan, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(chapter.storyIntro, color = NeoTextPrimary, fontSize = 13.sp, lineHeight = 20.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("LEVELS", color = NeoTextSecondary, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Spacer(Modifier.height(12.dp))
            }

            itemsIndexed(chapter.levels) { idx, level ->
                val completedLevels = progress.chapterProgress[chapterId] ?: -1
                val isCompleted = progress.completedLevelIds.contains(level.id)
                val isUnlocked = idx == 0 || progress.completedLevelIds.contains(
                    chapter.levels.getOrNull(idx - 1)?.id ?: ""
                )

                LevelItem(
                    level = level,
                    index = idx,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    isLast = idx == chapter.levels.size - 1,
                    onClick = { if (isUnlocked) onLevelSelect(chapterId, idx) }
                )
            }
        }
    }
}

@Composable
private fun LevelItem(
    level: Level,
    index: Int,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val nodeColor = when {
        isCompleted -> NeoGreen
        isUnlocked  -> NeoCyan
        else        -> NeoTextMuted
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(nodeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = nodeColor, modifier = Modifier.size(18.dp))
                } else {
                    Text("${index + 1}", color = nodeColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(if (isCompleted) NeoGreen.copy(alpha = 0.4f) else NeoBorder)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Level card
        Card(
            onClick = onClick,
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) NeoSurface else NeoBackground
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        level.title,
                        color = if (isUnlocked) NeoTextPrimary else NeoTextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${level.objectives.size} objective${if (level.objectives.size != 1) "s" else ""}",
                        color = NeoTextSecondary, fontSize = 11.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("+${level.xpReward} XP", color = XpGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    if (!isUnlocked) {
                        Icon(Icons.Default.Lock, null, tint = NeoTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
