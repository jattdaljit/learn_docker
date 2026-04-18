package com.dmarts.learndocker.ui.screens.level

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.domain.model.StoryLine
import com.dmarts.learndocker.ui.components.*
import com.dmarts.learndocker.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelScreen(
    chapterId: String,
    levelIndex: Int,
    onBack: () -> Unit,
    onLevelComplete: (chapterId: String, levelIndex: Int, xpEarned: Int) -> Unit
) {
    val context = LocalContext.current
    val vm: LevelViewModel = viewModel(
        key = "$chapterId/$levelIndex",
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LevelViewModel(chapterId, levelIndex, (context.applicationContext as LearnDockerApp).container) as T
            }
        }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs = listOf("Mission", "Visualize")

    // Achievement toast
    state.newlyUnlockedAchievements.firstOrNull()?.let { achievement ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AchievementToast(achievement, onDismiss = vm::clearNewAchievements)
        }
    }

    // Level complete dialog
    if (state.isLevelComplete && state.storyPhase == StoryPhase.POST) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = NeoSurface,
            title = {
                Text("MISSION COMPLETE", color = NeoGreen, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            },
            text = {
                Column {
                    Text(state.level.title, color = NeoTextPrimary, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("+${state.level.xpReward} XP earned", color = XpGold, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    // Post story
                    state.level.postStoryLines.forEach { line ->
                        StoryLineItem(line)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onLevelComplete(chapterId, levelIndex, state.level.xpReward) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeoCyan, contentColor = NeoBackground)
                ) {
                    Text("CONTINUE")
                }
            }
        )
    }

    // Pre-story dialog
    if (state.showStory && state.storyPhase == StoryPhase.PRE && state.level.preStoryLines.isNotEmpty()) {
        var storyIdx by remember { mutableIntStateOf(0) }
        val line = state.level.preStoryLines.getOrNull(storyIdx)
        if (line != null) {
            AlertDialog(
                onDismissRequest = vm::dismissStory,
                containerColor = NeoSurface,
                title = {
                    Text(state.level.title, color = NeoCyan, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.level.preStoryLines.take(storyIdx + 1).forEach { sl ->
                            StoryLineItem(sl)
                        }
                    }
                },
                confirmButton = {
                    if (storyIdx < state.level.preStoryLines.size - 1) {
                        TextButton(onClick = { storyIdx++ }) {
                            Text("Next ▶", color = NeoCyan)
                        }
                    } else {
                        Button(
                            onClick = vm::dismissStory,
                            colors = ButtonDefaults.buttonColors(containerColor = NeoCyan, contentColor = NeoBackground)
                        ) {
                            Text("START MISSION")
                        }
                    }
                }
            )
        }
    }

    var showHowToPlay by remember { mutableStateOf(false) }
    var objectivesExpanded by remember { mutableStateOf(true) }
    val isWide = LocalConfiguration.current.screenWidthDp >= 600

    if (showHowToPlay) {
        HowToPlayDialog(onDismiss = { showHowToPlay = false })
    }

    Scaffold(
        containerColor = NeoBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeoTextSecondary)
                    }
                },
                title = {
                    Column {
                        Text(
                            "Ch.${chapterId.substringAfter("ch_").toIntOrNull() ?: ""} · L${levelIndex + 1}",
                            color = NeoTextSecondary, fontSize = 10.sp
                        )
                        Text(state.level.title, color = NeoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                actions = {
                    Text(
                        "${state.validationResult.completedObjectiveIds.size}/${state.level.objectives.size}",
                        color = NeoCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(onClick = { showHowToPlay = true }) {
                        Icon(Icons.Default.Help, "How to play", tint = NeoTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        if (isWide) {
            // ── Tablet: mission panel left, terminal right ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .background(NeoBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeoSurface)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MISSION  ${state.validationResult.completedObjectiveIds.size}/${state.level.objectives.size}",
                            color = NeoCyan, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                    }
                    HorizontalDivider(color = NeoBorder)
                    MissionTab(state, vm)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(NeoBorder)
                )
                TerminalView(
                    lines = state.terminalLines,
                    input = state.currentInput,
                    onInputChange = vm::onInputChanged,
                    onSubmit = vm::onCommandSubmitted,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        } else {
            // ── Phone: collapsible mission top, terminal bottom ────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Tab row + collapse toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeoSurface),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.weight(1f),
                        containerColor = NeoSurface,
                        contentColor = NeoCyan
                    ) {
                        tabs.forEachIndexed { i, title ->
                            Tab(
                                selected = pagerState.currentPage == i,
                                onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                                text = { Text(title, fontSize = 13.sp) },
                                selectedContentColor = NeoCyan,
                                unselectedContentColor = NeoTextSecondary
                            )
                        }
                    }
                    IconButton(onClick = { objectivesExpanded = !objectivesExpanded }) {
                        Icon(
                            if (objectivesExpanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (objectivesExpanded) "Collapse mission" else "Expand mission",
                            tint = NeoTextSecondary
                        )
                    }
                }

                // Collapsible mission pager
                AnimatedVisibility(
                    visible = objectivesExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    ) { page ->
                        when (page) {
                            0 -> MissionTab(state, vm)
                            1 -> Box(modifier = Modifier.fillMaxSize()) {
                                SimulatorCanvas(
                                    state = state.simulatorState,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = NeoBorder)

                // Terminal — expands to fill all space when mission is collapsed
                TerminalView(
                    lines = state.terminalLines,
                    input = state.currentInput,
                    onInputChange = vm::onInputChanged,
                    onSubmit = vm::onCommandSubmitted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MissionTab(state: LevelUiState, vm: LevelViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ObjectivePanel(
            objectives = state.level.objectives,
            validationResult = state.validationResult,
            hints = state.level.hints,
            hintsUsed = state.hintsUsed,
            onRequestHint = vm::requestHint
        )
    }
}

@Composable
private fun StoryLineItem(line: StoryLine) {
    val color = Color(line.speakerColorHex)
    Column {
        Text(line.speaker, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(line.text, color = NeoTextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NeoSurface,
        title = {
            Text("How to Play", color = NeoCyan, fontWeight = FontWeight.Bold)
        },
        text = {
            androidx.compose.foundation.rememberScrollState().let { scroll ->
                Column(
                    modifier = Modifier.verticalScroll(scroll),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HowToPlayStep("1", "Read the Mission", NeoCyan,
                        "Tap the Mission tab at the top. It shows the objectives — the Docker commands you need to run to complete this level.")
                    HowToPlayStep("2", "Type in the Terminal", NeoGreen,
                        "The bottom half is your Docker terminal. Tap the input field, type a command, and press ↵ or the send button to run it.")
                    HowToPlayStep("3", "Complete All Objectives", NeoAmber,
                        "Each objective turns green (✓) when you've completed it. Complete all of them to finish the level.")
                    HowToPlayStep("4", "Use Hints", NeoPurple,
                        "Stuck? Tap 'Show Hint' in the Mission tab. Each hint gives you a clue about which command to use next.")
                    HowToPlayStep("5", "Visualize Your Work", Color(0xFF0EA5E9),
                        "Tap the Visualize tab to see your running containers, images, networks, and volumes displayed as cards.")

                    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                    Text("Useful Commands to Know:", color = NeoTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D1117))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(
                            "docker help" to "see all available commands",
                            "docker ps" to "list running containers",
                            "docker images" to "list downloaded images",
                            "docker run" to "create and start a container"
                        ).forEach { (cmd, desc) ->
                            Row {
                                Text(cmd, color = Color(0xFF7DD3FC), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, modifier = Modifier.width(130.dp))
                                Text("— $desc", color = Color(0xFF64748B), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeoCyan, contentColor = NeoBackground)
            ) { Text("Got it!") }
        }
    )
}

@Composable
private fun HowToPlayStep(number: String, title: String, color: Color, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = NeoTextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
