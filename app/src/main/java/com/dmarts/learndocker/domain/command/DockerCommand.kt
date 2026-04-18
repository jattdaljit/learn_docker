package com.dmarts.learndocker.domain.command

sealed class DockerCommand {
    data class Run(
        val image: String,
        val name: String? = null,
        val detached: Boolean = false,
        val ports: List<PortMapping> = emptyList(),
        val envVars: Map<String, String> = emptyMap(),
        val volumes: List<VolumeMapping> = emptyList(),
        val network: String? = null,
        val rm: Boolean = false,
        val interactive: Boolean = false,
        val command: List<String> = emptyList()
    ) : DockerCommand()

    data class Ps(val all: Boolean = false) : DockerCommand()
    data class Stop(val targets: List<String>) : DockerCommand()
    data class Start(val targets: List<String>) : DockerCommand()
    data class Rm(val targets: List<String>, val force: Boolean = false) : DockerCommand()
    data class Pull(val image: String, val tag: String = "latest") : DockerCommand()
    data class Images(val repository: String? = null) : DockerCommand()
    data class Rmi(val targets: List<String>, val force: Boolean = false) : DockerCommand()
    data class Exec(val container: String, val interactive: Boolean = false, val command: List<String>) : DockerCommand()
    data class Logs(val container: String, val follow: Boolean = false, val tail: Int? = null) : DockerCommand()
    data class Inspect(val targets: List<String>) : DockerCommand()
    data class Build(val tag: String? = null, val contextPath: String = ".") : DockerCommand()
    data class Tag(val source: String, val target: String) : DockerCommand()

    data class VolumeCreate(val name: String) : DockerCommand()
    object VolumeLs : DockerCommand()
    data class VolumeRm(val names: List<String>) : DockerCommand()
    data class VolumeInspect(val name: String) : DockerCommand()

    data class NetworkCreate(val name: String, val driver: String = "bridge") : DockerCommand()
    object NetworkLs : DockerCommand()
    data class NetworkConnect(val network: String, val container: String) : DockerCommand()
    data class NetworkDisconnect(val network: String, val container: String) : DockerCommand()
    data class NetworkRm(val names: List<String>) : DockerCommand()
    data class NetworkInspect(val name: String) : DockerCommand()

    object ContainerPrune : DockerCommand()
    data class ImagePrune(val all: Boolean = false) : DockerCommand()
    data class SystemPrune(val all: Boolean = false, val withVolumes: Boolean = false) : DockerCommand()

    object Info : DockerCommand()
    object Version : DockerCommand()

    data class ComposeUp(val detached: Boolean = false, val service: String? = null, val build: Boolean = false) : DockerCommand()
    data class ComposeDown(val withVolumes: Boolean = false) : DockerCommand()
    object ComposePs : DockerCommand()
    data class ComposeLogs(val service: String? = null) : DockerCommand()
    data class ComposeScale(val service: String, val replicas: Int) : DockerCommand()

    object Clear : DockerCommand()
    object Help : DockerCommand()
}

data class PortMapping(val hostPort: Int, val containerPort: Int, val protocol: String = "tcp")
data class VolumeMapping(val source: String, val target: String, val readOnly: Boolean = false)

sealed class ParseResult {
    data class Success(val command: DockerCommand) : ParseResult()
    data class Failure(val errorMessage: String) : ParseResult()
}
