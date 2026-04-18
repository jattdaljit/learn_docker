package com.dmarts.learndocker.domain.engine

import com.dmarts.learndocker.domain.model.SimulatorState

sealed class CommandResult {
    data class Success(
        val outputLines: List<String>,
        val newState: SimulatorState,
        val xpEarned: Int = 0
    ) : CommandResult()

    data class Error(
        val message: String,
        val newState: SimulatorState
    ) : CommandResult()

    val state: SimulatorState get() = when (this) {
        is Success -> newState
        is Error -> newState
    }

    val lines: List<String> get() = when (this) {
        is Success -> outputLines
        is Error -> listOf(message)
    }

    val isError: Boolean get() = this is Error
}
