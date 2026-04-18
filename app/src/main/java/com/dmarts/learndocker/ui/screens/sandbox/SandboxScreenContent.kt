package com.dmarts.learndocker.ui.screens.sandbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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

/**
 * Sandbox content without its own Scaffold — used as a tab in MainScreen.
 */
@Composable
fun SandboxScreenContent() {
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

    Column(modifier = Modifier.fillMaxSize()) {

        // Mini top bar (no back button — we're in a tab)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.weight(1f),
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
            IconButton(onClick = vm::resetState) {
                Icon(Icons.Default.Refresh, "Reset", tint = NeoAmber)
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
                    onSuggestionSelected = { vm.onInputChanged(it) },
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
