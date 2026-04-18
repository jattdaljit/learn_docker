package com.dmarts.learndocker.ui.screens.sandbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmarts.learndocker.AppContainer
import com.dmarts.learndocker.domain.command.DockerCommand
import com.dmarts.learndocker.domain.command.ParseResult
import com.dmarts.learndocker.domain.model.SimulatorState
import com.dmarts.learndocker.domain.model.TerminalLine
import com.dmarts.learndocker.domain.model.TerminalLineType
import com.dmarts.learndocker.domain.model.toTerminalLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SandboxUiState(
    val simulatorState: SimulatorState = SimulatorState(),
    val terminalLines: List<TerminalLine> = emptyList(),
    val currentInput: String = "",
    val suggestions: List<String> = ALL_SUGGESTIONS.take(6)
)

class SandboxViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(
        SandboxUiState(
            terminalLines = listOf(
                TerminalLine("Docker Sandbox", TerminalLineType.SYSTEM),
                TerminalLine("All commands available. No objectives.", TerminalLineType.SYSTEM),
                TerminalLine("Tap a suggestion or type a command below.", TerminalLineType.SYSTEM),
                TerminalLine("State is persisted across sessions. Type 'clear' to reset.", TerminalLineType.SYSTEM),
                TerminalLine("", TerminalLineType.SYSTEM),
            )
        )
    )
    val state: StateFlow<SandboxUiState> = _state.asStateFlow()

    init {
        // Restore persisted simulator state from previous session
        viewModelScope.launch {
            container.simulatorStateDataStore.stateFlow.first()?.let { saved: SimulatorState ->
                _state.update { it.copy(
                    simulatorState = saved,
                    terminalLines = it.terminalLines + listOf(
                        TerminalLine("Session restored — previous containers & images loaded.", TerminalLineType.SYSTEM),
                        TerminalLine("", TerminalLineType.SYSTEM),
                    )
                )}
            }
        }
    }

    fun onInputChanged(text: String) {
        _state.update { it.copy(currentInput = text, suggestions = computeSuggestions(text)) }
    }

    fun onCommandSubmitted() {
        val input = _state.value.currentInput.trim()
        if (input.isEmpty()) return

        _state.update {
            it.copy(
                currentInput = "",
                suggestions = ALL_SUGGESTIONS.take(6),
                terminalLines = it.terminalLines + TerminalLine(input, TerminalLineType.INPUT)
            )
        }

        if (input.lowercase() == "clear" || input.lowercase() == "cls") {
            _state.update { it.copy(terminalLines = emptyList()) }
            return
        }

        val parseResult = container.commandParser.parse(input)
        when (parseResult) {
            is ParseResult.Failure -> _state.update {
                it.copy(terminalLines = it.terminalLines +
                    TerminalLine(parseResult.errorMessage, TerminalLineType.ERROR))
            }
            is ParseResult.Success -> {
                val result = container.dockerSimulator.execute(
                    parseResult.command, _state.value.simulatorState
                )
                val newLines = result.lines.map { it.toTerminalLine(result.isError) }
                _state.update { it.copy(simulatorState = result.state, terminalLines = it.terminalLines + newLines) }
                viewModelScope.launch {
                    if (parseResult.command !is DockerCommand.Clear && parseResult.command !is DockerCommand.Help) {
                        container.progressRepository.update { p ->
                            p.copy(sandboxCommandsExecuted = p.sandboxCommandsExecuted + 1)
                        }
                        // Persist simulator state so containers/images survive app restart
                        container.simulatorStateDataStore.save(result.state)
                    }
                }
            }
        }
    }

    fun resetState() {
        _state.update {
            SandboxUiState(
                terminalLines = listOf(
                    TerminalLine("Sandbox reset. All containers and images cleared.", TerminalLineType.SYSTEM),
                    TerminalLine("", TerminalLineType.SYSTEM),
                )
            )
        }
        viewModelScope.launch { container.simulatorStateDataStore.clear() }
    }

    private fun computeSuggestions(input: String): List<String> {
        if (input.isBlank()) return ALL_SUGGESTIONS.take(6)
        val lower = input.lowercase()
        val exact = ALL_SUGGESTIONS.filter { it.startsWith(lower) }.take(8)
        return exact.ifEmpty { ALL_SUGGESTIONS.filter { it.contains(lower) }.take(4) }
    }
}

private val ALL_SUGGESTIONS = listOf(
    "docker ps",
    "docker ps -a",
    "docker run -d --name my-app nginx",
    "docker run -d -p 8080:80 --name web nginx",
    "docker run -d --name db -e POSTGRES_PASSWORD=secret postgres",
    "docker images",
    "docker pull nginx",
    "docker pull redis",
    "docker pull ubuntu",
    "docker pull postgres",
    "docker stop ",
    "docker start ",
    "docker rm ",
    "docker rm -f ",
    "docker rmi ",
    "docker exec -it  sh",
    "docker logs ",
    "docker logs -f ",
    "docker inspect ",
    "docker build -t myapp:latest .",
    "docker volume create mydata",
    "docker volume ls",
    "docker network create mynet",
    "docker network ls",
    "docker network connect mynet ",
    "docker container prune",
    "docker image prune",
    "docker system prune",
    "docker info",
    "docker help",
    "docker-compose up -d",
    "docker-compose down",
    "docker-compose ps",
    "docker-compose logs",
)
