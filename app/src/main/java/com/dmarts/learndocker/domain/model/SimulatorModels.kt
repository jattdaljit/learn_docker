package com.dmarts.learndocker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SimulatorState(
    val containers: List<DockerContainer> = emptyList(),
    val images: List<DockerImage> = emptyList(),
    val networks: List<DockerNetwork> = listOf(
        DockerNetwork("bridge", "bridge", "172.17.0.0/16"),
        DockerNetwork("host", "host", ""),
        DockerNetwork("none", "null", ""),
    ),
    val volumes: List<DockerVolume> = emptyList(),
    val composeStacks: List<ComposeStack> = emptyList(),
    val nextIpSuffix: Int = 2
)

@Serializable
data class DockerContainer(
    val id: String,
    val name: String,
    val imageRef: String,
    val status: ContainerStatus,
    val ports: List<PortBinding> = emptyList(),
    val envVars: Map<String, String> = emptyMap(),
    val volumeMounts: List<VolumeMount> = emptyList(),
    val networkName: String = "bridge",
    val ipAddress: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
)

@Serializable
enum class ContainerStatus { RUNNING, STOPPED, PAUSED, CREATED }

@Serializable
data class PortBinding(val hostPort: Int, val containerPort: Int, val protocol: String = "tcp")

@Serializable
data class VolumeMount(val volumeName: String, val mountPath: String)

@Serializable
data class DockerImage(
    val id: String,
    val repository: String,
    val tag: String,
    val sizeBytes: Long = 50_000_000L,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Serializable
data class DockerNetwork(
    val name: String,
    val driver: String,
    val subnet: String,
    val connectedContainerIds: List<String> = emptyList()
)

@Serializable
data class DockerVolume(
    val name: String,
    val driver: String = "local",
    val mountpoint: String = "/var/lib/docker/volumes/$name/_data"
)

@Serializable
data class ComposeStack(
    val name: String = "app",
    val services: List<ComposeService> = emptyList()
)

@Serializable
data class ComposeService(
    val name: String,
    val image: String,
    val scale: Int = 1,
    val containerIds: List<String> = emptyList()
)

data class TerminalLine(
    val text: String,
    val type: TerminalLineType,
    val timestampMs: Long = System.currentTimeMillis()
)

enum class TerminalLineType {
    INPUT, OUTPUT, ERROR, SYSTEM, SUCCESS, EXPLAIN,
    TABLE_HEADER,  // ── SECTION (N) ── divider-style header
    ACTION_OK,     // ✓ success/action confirmation line
    ID_LINE,       // muted container/image ID
    PULL_LINE      // ↓ pull layer progress
}

// Control-char prefixes emitted by DockerSimulator → stripped here and mapped to type
private const val PFX_H  = "\u0011"
private const val PFX_OK = "\u0012"
private const val PFX_ID = "\u0013"
private const val PFX_PL = "\u0014"

fun String.toTerminalLine(isError: Boolean = false): TerminalLine = when {
    startsWith(PFX_H)  -> TerminalLine(drop(1), TerminalLineType.TABLE_HEADER)
    startsWith(PFX_OK) -> TerminalLine(drop(1), TerminalLineType.ACTION_OK)
    startsWith(PFX_ID) -> TerminalLine(drop(1), TerminalLineType.ID_LINE)
    startsWith(PFX_PL) -> TerminalLine(drop(1), TerminalLineType.PULL_LINE)
    else -> TerminalLine(this, if (isError) TerminalLineType.ERROR else TerminalLineType.OUTPUT)
}
