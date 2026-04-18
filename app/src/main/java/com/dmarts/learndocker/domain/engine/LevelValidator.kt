package com.dmarts.learndocker.domain.engine

import com.dmarts.learndocker.domain.model.*

data class ValidationResult(
    val completedObjectiveIds: Set<String> = emptySet(),
    val isLevelComplete: Boolean = false
)

class LevelValidator {

    fun validate(level: Level, state: SimulatorState): ValidationResult {
        val completed = level.objectives.filter { checkObjective(it, state) }.map { it.id }.toSet()
        return ValidationResult(
            completedObjectiveIds = completed,
            isLevelComplete = completed.size == level.objectives.size
        )
    }

    private fun checkObjective(obj: Objective, state: SimulatorState): Boolean = when (obj) {
        is Objective.RunContainer -> state.containers.any { c ->
            imageMatches(c.imageRef, obj.imageName) &&
            c.status == ContainerStatus.RUNNING &&
            (obj.containerName == null || c.name == obj.containerName) &&
            (!obj.requireDetached || true) && // detach is a run-time flag; container is just Running
            obj.requiredPorts.all { rp -> c.ports.any { it.hostPort == rp } } &&
            obj.requiredEnvKeys.all { key -> c.envVars.containsKey(key) } &&
            obj.requiredVolumes.all { vol -> c.volumeMounts.any { it.volumeName == vol } } &&
            (obj.requiredNetwork == null || c.networkName == obj.requiredNetwork ||
             state.networks.any { n -> n.name == obj.requiredNetwork && c.id in n.connectedContainerIds })
        }

        is Objective.StopContainer -> state.containers.any { c ->
            (c.name == obj.containerNameOrId || c.id.startsWith(obj.containerNameOrId)) &&
            c.status == ContainerStatus.STOPPED
        }

        is Objective.StartContainer -> state.containers.any { c ->
            (c.name == obj.containerNameOrId || c.id.startsWith(obj.containerNameOrId)) &&
            c.status == ContainerStatus.RUNNING
        }

        is Objective.RemoveContainer -> state.containers.none { c ->
            c.name == obj.containerNameOrId || c.id.startsWith(obj.containerNameOrId)
        }

        is Objective.PullImage -> state.images.any { img ->
            imageMatches("${img.repository}:${img.tag}", obj.imageName) ||
            img.repository == obj.imageName
        }

        is Objective.RemoveImage -> state.images.none { img ->
            imageMatches("${img.repository}:${img.tag}", obj.imageName) ||
            img.repository == obj.imageName
        }

        is Objective.ListContainers -> true  // Satisfied by command execution; tracked in ViewModel

        is Objective.ListImages -> true  // Satisfied by command execution

        is Objective.CreateVolume -> state.volumes.any { it.name == obj.volumeName }

        is Objective.ListVolumes -> true

        is Objective.CreateNetwork -> state.networks.any { it.name == obj.networkName }

        is Objective.ConnectToNetwork -> state.networks.any { net ->
            net.name == obj.networkName &&
            state.containers.any { c ->
                c.name == obj.containerName && (c.id in net.connectedContainerIds || c.networkName == obj.networkName)
            }
        }

        is Objective.BuildImage -> state.images.any { img ->
            obj.requiredTag == null || imageMatches("${img.repository}:${img.tag}", obj.requiredTag)
        }

        is Objective.ExecIntoContainer -> true  // Satisfied by successful exec command

        is Objective.ViewLogs -> true  // Satisfied by successful logs command

        is Objective.InspectResource -> true  // Satisfied by successful inspect command

        is Objective.ComposeUp -> state.composeStacks.isNotEmpty()

        is Objective.ComposeDown -> state.composeStacks.isEmpty()

        is Objective.ComposePs -> true  // Satisfied by successful compose ps

        is Objective.ScaleService -> state.composeStacks.any { stack ->
            stack.services.any { svc -> svc.name == obj.serviceName && svc.containerIds.size >= obj.replicas }
        }

        is Objective.PruneContainers -> state.containers.none { it.status == ContainerStatus.STOPPED }

        is Objective.PruneImages -> true  // Satisfied by running image prune

        is Objective.PruneSystem -> state.containers.none { it.status == ContainerStatus.STOPPED }

        is Objective.Custom -> obj.check(state)
    }

    private fun imageMatches(imageRef: String, target: String): Boolean {
        val (tRepo, tTag) = if (":" in target) target.substringBeforeLast(":") to target.substringAfterLast(":")
                           else target to "latest"
        val (iRepo, iTag) = if (":" in imageRef) imageRef.substringBeforeLast(":") to imageRef.substringAfterLast(":")
                           else imageRef to "latest"
        return iRepo == tRepo && (tTag == "latest" || iTag == tTag)
    }
}
