package com.dmarts.learndocker.ui.screens.sandbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmarts.learndocker.LearnDockerApp
import com.dmarts.learndocker.ui.components.ResourceQuickCopyBar
import com.dmarts.learndocker.ui.components.SimulatorCanvas
import com.dmarts.learndocker.ui.components.TerminalView
import com.dmarts.learndocker.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandboxScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val vm: SandboxViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SandboxViewModel((context.applicationContext as LearnDockerApp).container) as T
        }
    })
    val state by vm.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

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
                        Text("Sandbox", color = NeoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Free experimentation", color = NeoTextSecondary, fontSize = 12.sp)
                    }
                },
                actions = {
                    IconButton(onClick = vm::resetState) {
                        Icon(Icons.Default.Refresh, "Reset", tint = NeoAmber)
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
                .imePadding()
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = NeoSurface,
                contentColor = NeoCyan
            ) {
                listOf("Terminal", "Visualize").forEachIndexed { i, title ->
                    Tab(
                        selected = pagerState.currentPage == i,
                        onClick = { scope.launch { pagerState.animateScrollToPage(i) } },
                        text = { Text(title, fontSize = 13.sp) },
                        selectedContentColor = NeoCyan,
                        unselectedContentColor = NeoTextSecondary
                    )
                }
            }

            // Quick-copy resource bar: shown only on Terminal tab
            if (pagerState.currentPage == 0) {
                ResourceQuickCopyBar(
                    state = state.simulatorState,
                    onInsert = { name ->
                        val current = state.currentInput
                        vm.onInputChanged(if (current.isBlank()) name else "$current $name")
                    }
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> TerminalView(
                        lines = state.terminalLines,
                        input = state.currentInput,
                        onInputChange = vm::onInputChanged,
                        onSubmit = vm::onCommandSubmitted,
                        suggestions = state.suggestions,
                        onSuggestionSelected = { suggestion ->
                            vm.onInputChanged(suggestion)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> SimulatorCanvas(
                        state = state.simulatorState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
