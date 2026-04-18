package com.dmarts.learndocker.ui.screens.level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmarts.learndocker.AppContainer
import com.dmarts.learndocker.domain.command.DockerCommand
import com.dmarts.learndocker.domain.command.ParseResult
import com.dmarts.learndocker.domain.engine.CommandResult
import com.dmarts.learndocker.domain.engine.ValidationResult
import com.dmarts.learndocker.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class LevelUiState(
    val level: Level = Level("", "", 0, "", emptyList(), emptyList(), emptyList(), emptyList(), 0),
    val simulatorState: SimulatorState = SimulatorState(),
    val terminalLines: List<TerminalLine> = emptyList(),
    val currentInput: String = "",
    val validationResult: ValidationResult = ValidationResult(),
    val manuallyCompletedObjectiveIds: Set<String> = emptySet(),
    val newlyUnlockedAchievements: List<AchievementDef> = emptyList(),
    val isLevelComplete: Boolean = false,
    val commandCount: Int = 0,
    val hintsUsed: Int = 0,
    val showMissionPanel: Boolean = false,
    val showStory: Boolean = true,
    val storyPhase: StoryPhase = StoryPhase.PRE
)

enum class StoryPhase { PRE, LEVEL, POST, COMPLETE }

class LevelViewModel(
    private val chapterId: String,
    private val levelIndex: Int,
    private val container: AppContainer,
    private val isSandbox: Boolean = false
) : ViewModel() {

    private val _state = MutableStateFlow(LevelUiState())
    val state: StateFlow<LevelUiState> = _state.asStateFlow()

    private var currentProgress = UserProgress()

    init {
        val level = container.chapterRegistry.byId(chapterId)?.levels?.getOrNull(levelIndex)
        if (level != null) {
            _state.update { it.copy(
                level = level,
                simulatorState = level.initialState,
                terminalLines = listOf(
                    TerminalLine("Docker Engine — ready", TerminalLineType.SYSTEM),
                    TerminalLine("Type 'docker help' for available commands.", TerminalLineType.SYSTEM),
                    TerminalLine("", TerminalLineType.SYSTEM),
                )
            )}
        }
        viewModelScope.launch {
            container.progressRepository.progressFlow.collect { progress ->
                currentProgress = progress
            }
        }
    }

    fun onInputChanged(text: String) = _state.update { it.copy(currentInput = text) }

    fun onCommandSubmitted() {
        val input = _state.value.currentInput.trim()
        if (input.isEmpty()) return

        val inputLine = TerminalLine(input, TerminalLineType.INPUT)
        _state.update { it.copy(currentInput = "", terminalLines = it.terminalLines + inputLine) }

        val parseResult = container.commandParser.parse(input)
        when (parseResult) {
            is ParseResult.Failure -> {
                val errLine = TerminalLine(parseResult.errorMessage, TerminalLineType.ERROR)
                _state.update { it.copy(terminalLines = it.terminalLines + errLine) }
            }
            is ParseResult.Success -> {
                val cmd = parseResult.command
                val currentState = _state.value.simulatorState
                val result = container.dockerSimulator.execute(cmd, currentState)
                val outputLines = result.lines.map { it.toTerminalLine(result.isError) }
                val explanation = if (!result.isError) commandExplanation(cmd) else null
                val newLines = outputLines + listOfNotNull(
                    explanation?.let { TerminalLine(it, TerminalLineType.EXPLAIN) }
                )

                val newSimState = result.state
                val level = _state.value.level
                val stateValidation = container.levelValidator.validate(level, newSimState)

                // Track action-based objectives (only mark them if the command succeeded)
                val newManualIds = if (!result.isError) {
                    _state.value.manuallyCompletedObjectiveIds +
                        level.objectives
                            .filter { matchesActionObjective(it, cmd) }
                            .map { it.id }
                } else {
                    _state.value.manuallyCompletedObjectiveIds
                }

                // Merge state-based + action-based completions
                val allCompleted = stateValidation.completedObjectiveIds + newManualIds
                val mergedValidation = ValidationResult(
                    completedObjectiveIds = allCompleted,
                    isLevelComplete = allCompleted.size == level.objectives.size
                )

                val updatedProgress = currentProgress.copy(
                    commandsExecuted = currentProgress.commandsExecuted + 1,
                    containersCreated = if (cmd is DockerCommand.Run)
                        currentProgress.containersCreated + 1 else currentProgress.containersCreated
                )

                val newAchievements = container.achievementEngine.evaluate(updatedProgress)
                val progressWithAchievements = updatedProgress.copy(
                    unlockedAchievementIds = updatedProgress.unlockedAchievementIds + newAchievements.map { it.id }
                )

                _state.update { s ->
                    s.copy(
                        simulatorState = newSimState,
                        terminalLines = s.terminalLines + newLines,
                        validationResult = mergedValidation,
                        manuallyCompletedObjectiveIds = newManualIds,
                        newlyUnlockedAchievements = newAchievements,
                        isLevelComplete = mergedValidation.isLevelComplete && !s.isLevelComplete,
                        commandCount = s.commandCount + 1,
                        storyPhase = if (mergedValidation.isLevelComplete) StoryPhase.POST else StoryPhase.LEVEL
                    )
                }

                viewModelScope.launch {
                    container.progressRepository.save(progressWithAchievements)
                    if (mergedValidation.isLevelComplete) {
                        saveLevelComplete(level, result)
                    }
                }
            }
        }
    }

    fun requestHint() {
        val level = _state.value.level
        val hintIdx = _state.value.hintsUsed % level.hints.size
        if (level.hints.isEmpty()) return
        val hint = level.hints[hintIdx]
        val hintLine = TerminalLine("HINT: $hint", TerminalLineType.SYSTEM)
        _state.update { it.copy(
            terminalLines = it.terminalLines + hintLine,
            hintsUsed = it.hintsUsed + 1
        )}
    }

    private fun commandExplanation(cmd: DockerCommand): String? = when (cmd) {
        is DockerCommand.Run -> buildString {
            append("docker run creates and starts a new container from an image.")
            if (cmd.detached) append(" -d runs it in the background (detached mode).")
            if (cmd.name != null) append(" --name sets the container name.")
            if (cmd.ports.isNotEmpty()) append(" -p maps host:container ports so the app is reachable.")
            if (cmd.envVars.isNotEmpty()) append(" -e passes environment variables into the container.")
            if (cmd.volumes.isNotEmpty()) append(" -v mounts a volume to persist data.")
            if (cmd.network != null) append(" --network connects the container to a custom network.")
        }
        is DockerCommand.Ps ->
            if (cmd.all) "docker ps -a lists ALL containers (running + stopped). Without -a only running ones show."
            else "docker ps lists running containers — their ID, image, status, ports, and name."
        is DockerCommand.Stop ->
            "docker stop gracefully shuts down a container by sending SIGTERM, waiting 10s, then SIGKILL."
        is DockerCommand.Start ->
            "docker start restarts a stopped container, keeping its original config and any data inside."
        is DockerCommand.Rm ->
            "docker rm deletes a stopped container. Use -f to force-remove a running one. Data inside is lost."
        is DockerCommand.Pull ->
            "docker pull downloads an image from Docker Hub (or another registry). Images are cached locally."
        is DockerCommand.Images ->
            "docker images lists locally available images with their name, tag, ID, and disk size."
        is DockerCommand.Rmi ->
            "docker rmi removes an image from local storage. Cannot remove an image used by an existing container."
        is DockerCommand.Exec ->
            "docker exec runs a command inside a running container. -it gives an interactive terminal session."
        is DockerCommand.Logs ->
            if (cmd.follow) "docker logs -f streams live output from the container (like tail -f)."
            else "docker logs shows the stdout/stderr output a container has produced since it started."
        is DockerCommand.Inspect ->
            "docker inspect returns detailed JSON about a container, image, volume, or network — useful for debugging."
        is DockerCommand.Build ->
            "docker build reads a Dockerfile and creates a new image. -t gives it a name:tag. The path (.) is the build context."
        is DockerCommand.VolumeCreate ->
            "docker volume create makes a named volume managed by Docker. Volumes outlive containers and persist data."
        is DockerCommand.VolumeLs ->
            "docker volume ls lists all volumes on this Docker host. Volumes store persistent data outside containers."
        is DockerCommand.NetworkCreate ->
            "docker network create makes a new virtual network. Containers on the same network can reach each other by name."
        is DockerCommand.NetworkLs ->
            "docker network ls shows all networks. bridge is the default; host shares the host's network stack."
        is DockerCommand.NetworkConnect ->
            "docker network connect attaches a running container to an additional network without restarting it."
        is DockerCommand.ComposeUp ->
            "docker-compose up reads docker-compose.yml and starts all defined services. -d runs them in the background."
        is DockerCommand.ComposeDown ->
            "docker-compose down stops and removes all containers, networks, and (optionally) volumes created by compose up."
        is DockerCommand.ContainerPrune ->
            "docker container prune removes all stopped containers at once, freeing up disk space."
        is DockerCommand.ImagePrune ->
            "docker image prune removes unused images. Add -a to also remove images not referenced by any container."
        is DockerCommand.SystemPrune ->
            "docker system prune is a deep-clean: removes stopped containers, unused networks, dangling images, and build cache."
        else -> null
    }

    private fun matchesActionObjective(obj: Objective, cmd: DockerCommand): Boolean = when (obj) {
        is Objective.ListContainers -> cmd is DockerCommand.Ps
        is Objective.ListImages -> cmd is DockerCommand.Images
        is Objective.ListVolumes -> cmd is DockerCommand.VolumeLs
        is Objective.ExecIntoContainer -> cmd is DockerCommand.Exec &&
            cmd.container == obj.containerNameOrId
        is Objective.ViewLogs -> cmd is DockerCommand.Logs &&
            cmd.container == obj.containerNameOrId
        is Objective.InspectResource -> cmd is DockerCommand.Inspect &&
            cmd.targets.any { it == obj.targetNameOrId }
        is Objective.ComposePs -> cmd is DockerCommand.ComposePs
        is Objective.PruneImages -> cmd is DockerCommand.ImagePrune || cmd is DockerCommand.SystemPrune
        else -> false
    }

    fun clearNewAchievements() = _state.update { it.copy(newlyUnlockedAchievements = emptyList()) }

    fun dismissStory() = _state.update { it.copy(showStory = false, storyPhase = StoryPhase.LEVEL) }

    fun toggleMissionPanel() = _state.update { it.copy(showMissionPanel = !it.showMissionPanel) }

    private suspend fun saveLevelComplete(level: Level, result: CommandResult) {
        val xp = level.xpReward + (if (result is CommandResult.Success) result.xpEarned else 0)
        container.progressRepository.update { p ->
            val levelIdx = container.chapterRegistry.byId(level.chapterId)
                ?.levels?.indexOfFirst { it.id == level.id } ?: return@update p
            val prevMax = p.chapterProgress[level.chapterId] ?: -1
            p.copy(
                totalXp = p.totalXp + xp,
                completedLevelIds = p.completedLevelIds + level.id,
                chapterProgress = p.chapterProgress + (level.chapterId to maxOf(prevMax, levelIdx)),
                lastPlayedDateEpochDay = LocalDate.now().toEpochDay()
            )
        }
    }
}
