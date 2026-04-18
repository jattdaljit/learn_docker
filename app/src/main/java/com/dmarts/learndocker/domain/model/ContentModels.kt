package com.dmarts.learndocker.domain.model

data class Chapter(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String,
    val districtName: String,
    val storyIntro: String,
    val levels: List<Level>,
    val xpReward: Int = 0,
    val requiredChapterId: String? = null
)

data class Level(
    val id: String,
    val chapterId: String,
    val number: Int,
    val title: String,
    val preStoryLines: List<StoryLine>,
    val postStoryLines: List<StoryLine>,
    val objectives: List<Objective>,
    val hints: List<String>,
    val xpReward: Int,
    val challengeTimeSeconds: Int = 120,
    val initialState: SimulatorState = SimulatorState()
)

data class StoryLine(
    val speaker: String,
    val text: String,
    val speakerColorHex: Long = 0xFF00E5FF
)

sealed class Objective {
    abstract val id: String
    abstract val description: String

    data class RunContainer(
        override val id: String,
        override val description: String,
        val imageName: String,
        val containerName: String? = null,
        val requireDetached: Boolean = false,
        val requiredPorts: List<Int> = emptyList(),
        val requiredEnvKeys: List<String> = emptyList(),
        val requiredVolumes: List<String> = emptyList(),
        val requiredNetwork: String? = null
    ) : Objective()

    data class StopContainer(
        override val id: String,
        override val description: String,
        val containerNameOrId: String
    ) : Objective()

    data class StartContainer(
        override val id: String,
        override val description: String,
        val containerNameOrId: String
    ) : Objective()

    data class RemoveContainer(
        override val id: String,
        override val description: String,
        val containerNameOrId: String
    ) : Objective()

    data class PullImage(
        override val id: String,
        override val description: String,
        val imageName: String
    ) : Objective()

    data class RemoveImage(
        override val id: String,
        override val description: String,
        val imageName: String
    ) : Objective()

    data class ListContainers(
        override val id: String,
        override val description: String,
        val includeAll: Boolean = false
    ) : Objective()

    data class ListImages(
        override val id: String,
        override val description: String
    ) : Objective()

    data class CreateVolume(
        override val id: String,
        override val description: String,
        val volumeName: String
    ) : Objective()

    data class ListVolumes(
        override val id: String,
        override val description: String
    ) : Objective()

    data class CreateNetwork(
        override val id: String,
        override val description: String,
        val networkName: String
    ) : Objective()

    data class ConnectToNetwork(
        override val id: String,
        override val description: String,
        val containerName: String,
        val networkName: String
    ) : Objective()

    data class BuildImage(
        override val id: String,
        override val description: String,
        val requiredTag: String? = null
    ) : Objective()

    data class ExecIntoContainer(
        override val id: String,
        override val description: String,
        val containerNameOrId: String
    ) : Objective()

    data class ViewLogs(
        override val id: String,
        override val description: String,
        val containerNameOrId: String
    ) : Objective()

    data class InspectResource(
        override val id: String,
        override val description: String,
        val targetNameOrId: String
    ) : Objective()

    data class ComposeUp(
        override val id: String,
        override val description: String
    ) : Objective()

    data class ComposeDown(
        override val id: String,
        override val description: String
    ) : Objective()

    data class ComposePs(
        override val id: String,
        override val description: String
    ) : Objective()

    data class ScaleService(
        override val id: String,
        override val description: String,
        val serviceName: String,
        val replicas: Int
    ) : Objective()

    data class PruneContainers(
        override val id: String,
        override val description: String
    ) : Objective()

    data class PruneImages(
        override val id: String,
        override val description: String
    ) : Objective()

    data class PruneSystem(
        override val id: String,
        override val description: String
    ) : Objective()

    data class Custom(
        override val id: String,
        override val description: String,
        val check: (SimulatorState) -> Boolean
    ) : Objective()
}
