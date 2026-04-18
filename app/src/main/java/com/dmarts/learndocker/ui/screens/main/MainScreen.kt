package com.dmarts.learndocker.ui.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.ads.BannerAdView
import com.dmarts.learndocker.ads.InterstitialAdManager
import com.dmarts.learndocker.ads.RewardedAdManager
import com.dmarts.learndocker.domain.model.Chapter
import com.dmarts.learndocker.domain.model.UserProgress
import com.dmarts.learndocker.ui.components.XpBar
import com.dmarts.learndocker.ui.screens.commands.CommandsScreenContent
import com.dmarts.learndocker.ui.screens.home.HomeViewModel
import com.dmarts.learndocker.ui.screens.hub.DockerHubScreenContent
import com.dmarts.learndocker.ui.screens.sandbox.SandboxScreenContent
import com.dmarts.learndocker.ui.theme.*

// ─── Tab definitions ─────────────────────────────────────────────────────────

private enum class MainTab(val label: String, val icon: ImageVector) {
    LEARN("Learn", Icons.Default.School),
    PRACTICE("Practice", Icons.Default.Terminal),
    REFERENCE("Reference", Icons.Default.MenuBook),
    HUB("Hub", Icons.Default.Search)
}

// ─── Main shell ──────────────────────────────────────────────────────────────

@Composable
fun MainScreen(
    onChapterClick: (String) -> Unit,
    onAchievementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    // Preload ads when MainScreen first enters composition
    LaunchedEffect(Unit) {
        RewardedAdManager.preload(context)
        InterstitialAdManager.preload(context)
    }

    // Sandbox is locked behind a rewarded ad; unlocked once per session
    var sandboxUnlocked by remember { mutableStateOf(false) }
    var showRewardedDialog by remember { mutableStateOf(false) }
    var adLoading by remember { mutableStateOf(false) }

    val isWide = LocalConfiguration.current.screenWidthDp >= 600
    var selectedTab by remember { mutableStateOf(MainTab.LEARN) }

    // Show rewarded ad gate dialog
    if (showRewardedDialog) {
        AlertDialog(
            onDismissRequest = { showRewardedDialog = false },
            containerColor = NeoSurface,
            title = {
                Text("Unlock Sandbox", color = NeoTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Watch a short ad to unlock free sandbox experimentation.",
                    color = NeoTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (activity != null) {
                            adLoading = true
                            showRewardedDialog = false
                            val shown = RewardedAdManager.show(
                                activity = activity,
                                onRewarded = { sandboxUnlocked = true },
                                onDismissed = {
                                    adLoading = false
                                    if (sandboxUnlocked) selectedTab = MainTab.PRACTICE
                                }
                            )
                            if (!shown) {
                                // Ad not loaded yet — grant access anyway
                                sandboxUnlocked = true
                                adLoading = false
                                selectedTab = MainTab.PRACTICE
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoCyan, contentColor = NeoBackground)
                ) {
                    Text("Watch Ad", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRewardedDialog = false }) {
                    Text("Cancel", color = NeoTextSecondary)
                }
            }
        )
    }

    // Helper: handle tab click with rewarded ad gate for Practice
    val onTabSelected: (MainTab) -> Unit = { tab ->
        if (tab == MainTab.PRACTICE && !sandboxUnlocked) {
            showRewardedDialog = true
        } else {
            selectedTab = tab
        }
    }

    if (isWide) {
        // ── Tablet / foldable: NavigationRail on the left ─────────────────
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(NeoBackground)
                .statusBarsPadding()
        ) {
            NavigationRail(
                containerColor = NeoSurface,
                modifier = Modifier.fillMaxHeight().navigationBarsPadding()
            ) {
                Spacer(Modifier.weight(1f))
                MainTab.entries.forEach { tab ->
                    NavigationRailItem(
                        selected = selectedTab == tab,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Icon(tab.icon, contentDescription = tab.label,
                                modifier = Modifier.size(22.dp))
                        },
                        label = { Text(tab.label, fontSize = 11.sp) },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = NeoCyan,
                            selectedTextColor = NeoCyan,
                            unselectedIconColor = NeoTextSecondary,
                            unselectedTextColor = NeoTextSecondary,
                            indicatorColor = NeoCyan.copy(alpha = 0.12f)
                        )
                    )
                }
                Spacer(Modifier.weight(1f))
            }
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding(),
                label = "tab"
            ) { tab ->
                MainTabContent(tab, onChapterClick, onAchievementsClick, onProfileClick, onSettingsClick, sandboxUnlocked)
            }
        }
    } else {
        // ── Phone: bottom NavigationBar ───────────────────────────────────
        Scaffold(
            containerColor = NeoBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(
                    containerColor = NeoSurface,
                    tonalElevation = 0.dp
                ) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            icon = {
                                Icon(tab.icon, contentDescription = tab.label,
                                    modifier = Modifier.size(22.dp))
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeoCyan,
                                selectedTextColor = NeoCyan,
                                unselectedIconColor = NeoTextSecondary,
                                unselectedTextColor = NeoTextSecondary,
                                indicatorColor = NeoCyan.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()           // status bar not handled by Scaffold (no topBar)
                    .padding(padding)              // nav bar bottom from NavigationBar
                    .consumeWindowInsets(padding)  // prevent nav bar double-count in imePadding
                    .imePadding(),                 // keyboard only
                label = "tab"
            ) { tab ->
                MainTabContent(tab, onChapterClick, onAchievementsClick, onProfileClick, onSettingsClick, sandboxUnlocked)
            }
        }
    }
}

@Composable
private fun MainTabContent(
    tab: MainTab,
    onChapterClick: (String) -> Unit,
    onAchievementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    sandboxUnlocked: Boolean = false
) {
    when (tab) {
        MainTab.LEARN -> LearnTab(
            onChapterClick = onChapterClick,
            onAchievementsClick = onAchievementsClick,
            onProfileClick = onProfileClick,
            onSettingsClick = onSettingsClick
        )
        MainTab.PRACTICE -> if (sandboxUnlocked) {
            SandboxScreenContent()
        } else {
            SandboxLockedContent()
        }
        MainTab.REFERENCE -> CommandsScreenContent()
        MainTab.HUB -> DockerHubScreenContent()
    }
}

@Composable
private fun SandboxLockedContent() {
    Box(
        modifier = Modifier.fillMaxSize().background(NeoBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(Icons.Default.Lock, null, tint = NeoTextMuted, modifier = Modifier.size(52.dp))
            Text("Sandbox Locked", color = NeoTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Tap the Practice tab to watch a short ad and unlock the sandbox.",
                color = NeoTextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ─── Learn tab ───────────────────────────────────────────────────────────────

@Composable
private fun LearnTab(
    onChapterClick: (String) -> Unit,
    onAchievementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel((context.applicationContext as LearnDockerApp).container) as T
        }
    })
    val state by vm.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(NeoBackground)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // ── Header ──
            item {
                LearnHeader(
                    progress = state.progress,
                    playerTitle = state.playerTitle,
                    onAchievements = onAchievementsClick,
                    onProfile = onProfileClick,
                    onSettings = onSettingsClick
                )
            }

            // ── Resume card ──
            item {
                val nextChapter = state.chapters.firstOrNull { ch ->
                    (state.progress.chapterProgress[ch.id] ?: -1) < ch.levels.size - 1
                }
                if (nextChapter != null) {
                    ResumeCard(
                        chapter = nextChapter,
                        progress = state.progress,
                        vm = vm,
                        onClick = { onChapterClick(nextChapter.id) }
                    )
                }
            }

            // ── "What you'll learn" intro ──
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Learning Path", color = NeoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("${state.progress.completedLevelIds.size} / ${state.chapters.sumOf { it.levels.size }} completed",
                        color = NeoTextSecondary, fontSize = 12.sp)
                }
            }

            // ── Module list ──
            items(state.chapters) { chapter ->
                val unlocked = vm.isChapterUnlocked(chapter, state.progress)
                val completionPct = vm.getChapterCompletionPercent(chapter, state.progress)
                LearningModuleCard(
                    chapter = chapter,
                    unlocked = unlocked,
                    completionPct = completionPct,
                    onClick = { if (unlocked) onChapterClick(chapter.id) }
                )
            }
        }

        // ── Banner ad — always visible at bottom ──
        BannerAdView()
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun LearnHeader(
    progress: UserProgress,
    playerTitle: String,
    onAchievements: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NeoSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Docker Learn", color = NeoCyan, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(playerTitle, color = NeoTextSecondary, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            // Streak
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NeoRed.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = NeoRed, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(3.dp))
                Text("${progress.currentStreak}d", color = NeoRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(2.dp))
            IconButton(onClick = onAchievements, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.EmojiEvents, null, tint = XpGold, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onProfile, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Person, null, tint = NeoTextSecondary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onSettings, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Settings, null, tint = NeoTextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ─── Resume card ─────────────────────────────────────────────────────────────

@Composable
private fun ResumeCard(
    chapter: Chapter,
    progress: UserProgress,
    vm: HomeViewModel,
    onClick: () -> Unit
) {
    val pct = vm.getChapterCompletionPercent(chapter, progress)
    val completedLevels = ((progress.chapterProgress[chapter.id] ?: -1) + 1).coerceAtLeast(0)
    val nextLevel = completedLevels + 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NeoCyan.copy(alpha = 0.08f))
            .border(1.dp, NeoCyan.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NeoCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayCircle, null, tint = NeoCyan, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("Continue Learning", color = NeoCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(chapter.title, color = NeoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("Level $nextLevel · ${(pct * 100).toInt()}% complete",
                color = NeoTextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = NeoCyan,
                trackColor = NeoCyan.copy(alpha = 0.15f)
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Default.KeyboardArrowRight, null, tint = NeoCyan, modifier = Modifier.size(20.dp))
    }
}

// ─── Learning module card ─────────────────────────────────────────────────────

private val MODULE_META = mapOf(
    "ch_01" to ModuleMeta(
        topic = "First Day On The Job",
        description = "Run your first containers, list what's running, and manage the container lifecycle.",
        concepts = listOf("docker run", "docker ps", "docker stop", "docker start"),
        accentColor = Color(0xFF0EA5E9)
    ),
    "ch_02" to ModuleMeta(
        topic = "Incident Response",
        description = "Debug running services — check logs, exec inside containers, inspect metadata, and clean up.",
        concepts = listOf("docker logs", "docker exec", "docker inspect", "docker rm"),
        accentColor = Color(0xFF7C3AED)
    ),
    "ch_03" to ModuleMeta(
        topic = "Config, Data & Networking",
        description = "Inject secrets via env vars, persist data with volumes, and isolate services with networks.",
        concepts = listOf("env vars -e", "docker volume", "docker network", "network connect"),
        accentColor = Color(0xFFD97706)
    ),
    "ch_04" to ModuleMeta(
        topic = "Image Management",
        description = "Pull images from Docker Hub, choose the right tags, remove unused images to free disk space.",
        concepts = listOf("docker pull", "docker images", "docker rmi", "image tags"),
        accentColor = Color(0xFF0891B2)
    ),
    "ch_05" to ModuleMeta(
        topic = "Full Stack Deployment",
        description = "Launch a complete application stack — web, api, database, cache — with Docker Compose.",
        concepts = listOf("compose up", "compose down", "compose ps", "docker system prune"),
        accentColor = Color(0xFFE11D48)
    )
)

private data class ModuleMeta(
    val topic: String,
    val description: String,
    val concepts: List<String>,
    val accentColor: Color
)

@Composable
private fun LearningModuleCard(
    chapter: Chapter,
    unlocked: Boolean,
    completionPct: Float,
    onClick: () -> Unit
) {
    val meta = MODULE_META[chapter.id]
    val accent = if (unlocked) (meta?.accentColor ?: NeoCyan) else NeoTextMuted
    val statusText = when {
        completionPct >= 1f -> "Completed"
        completionPct > 0f -> "In progress"
        !unlocked -> "Locked"
        else -> "Not started"
    }
    val statusColor = when {
        completionPct >= 1f -> NeoGreen
        completionPct > 0f -> NeoCyan
        !unlocked -> NeoTextMuted
        else -> NeoTextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NeoSurface)
            .border(
                1.dp,
                if (completionPct > 0f && completionPct < 1f) accent.copy(alpha = 0.4f) else NeoBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = unlocked, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Module number circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = if (unlocked) 0.15f else 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            if (completionPct >= 1f) {
                Icon(Icons.Default.Check, null, tint = NeoGreen, modifier = Modifier.size(20.dp))
            } else if (!unlocked) {
                Icon(Icons.Default.Lock, null, tint = NeoTextMuted, modifier = Modifier.size(18.dp))
            } else {
                Text(
                    "${chapter.number}",
                    color = accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        meta?.topic ?: chapter.title,
                        color = if (unlocked) NeoTextPrimary else NeoTextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        chapter.subtitle,
                        color = NeoTextSecondary,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            if (unlocked && meta != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    meta.description,
                    color = NeoTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                // Concept chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(meta.concepts) { concept ->
                        Text(
                            concept,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accent.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            color = accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (completionPct > 0f) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { completionPct },
                        modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = if (completionPct >= 1f) NeoGreen else accent,
                        trackColor = accent.copy(alpha = 0.12f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${chapter.levels.size} levels",
                        color = NeoTextSecondary,
                        fontSize = 10.sp
                    )
                }
            } else if (unlocked) {
                Spacer(Modifier.height(6.dp))
                Text("${chapter.levels.size} levels", color = NeoTextSecondary, fontSize = 11.sp)
            }
        }
    }
}
