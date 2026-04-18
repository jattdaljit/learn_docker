package com.dmarts.learndocker.domain.engine

import com.dmarts.learndocker.domain.command.DockerCommand
import com.dmarts.learndocker.domain.command.PortMapping
import com.dmarts.learndocker.domain.command.VolumeMapping
import com.dmarts.learndocker.domain.model.*
import kotlin.random.Random

class DockerSimulator {

    // Typed-output prefix chars (stripped + mapped to TerminalLineType by ViewModels)
    private val H  = "\u0011"  // TABLE_HEADER
    private val OK = "\u0012"  // ACTION_OK
    private val ID = "\u0013"  // ID_LINE
    private val PL = "\u0014"  // PULL_LINE

    fun execute(cmd: DockerCommand, state: SimulatorState): CommandResult = when (cmd) {
        is DockerCommand.Run         -> doRun(cmd, state)
        is DockerCommand.Ps          -> doPs(cmd, state)
        is DockerCommand.Stop        -> doStop(cmd, state)
        is DockerCommand.Start       -> doStart(cmd, state)
        is DockerCommand.Rm          -> doRm(cmd, state)
        is DockerCommand.Pull        -> doPull(cmd, state)
        is DockerCommand.Images      -> doImages(cmd, state)
        is DockerCommand.Rmi         -> doRmi(cmd, state)
        is DockerCommand.Exec        -> doExec(cmd, state)
        is DockerCommand.Logs        -> doLogs(cmd, state)
        is DockerCommand.Inspect     -> doInspect(cmd, state)
        is DockerCommand.Build       -> doBuild(cmd, state)
        is DockerCommand.Tag         -> doTag(cmd, state)
        is DockerCommand.VolumeCreate -> doVolumeCreate(cmd, state)
        DockerCommand.VolumeLs       -> doVolumeLs(state)
        is DockerCommand.VolumeRm    -> doVolumeRm(cmd, state)
        is DockerCommand.VolumeInspect -> doVolumeInspect(cmd, state)
        is DockerCommand.NetworkCreate -> doNetworkCreate(cmd, state)
        DockerCommand.NetworkLs      -> doNetworkLs(state)
        is DockerCommand.NetworkConnect -> doNetworkConnect(cmd, state)
        is DockerCommand.NetworkDisconnect -> doNetworkDisconnect(cmd, state)
        is DockerCommand.NetworkRm   -> doNetworkRm(cmd, state)
        is DockerCommand.NetworkInspect -> doNetworkInspect(cmd, state)
        DockerCommand.ContainerPrune -> doContainerPrune(state)
        is DockerCommand.ImagePrune  -> doImagePrune(cmd, state)
        is DockerCommand.SystemPrune -> doSystemPrune(cmd, state)
        DockerCommand.Info           -> doInfo(state)
        DockerCommand.Version        -> doVersion(state)
        is DockerCommand.ComposeUp   -> doComposeUp(cmd, state)
        is DockerCommand.ComposeDown -> doComposeDown(cmd, state)
        DockerCommand.ComposePs      -> doComposePs(state)
        is DockerCommand.ComposeLogs -> doComposeLogs(cmd, state)
        is DockerCommand.ComposeScale -> doComposeScale(cmd, state)
        DockerCommand.Clear          -> CommandResult.Success(emptyList(), state)
        DockerCommand.Help           -> CommandResult.Success(HELP_LINES, state)
    }

    // ─── RUN ─────────────────────────────────────────────────────────────────

    private fun doRun(cmd: DockerCommand.Run, state: SimulatorState): CommandResult {
        if (cmd.image.isEmpty())
            return CommandResult.Error("docker: 'run' requires at least 1 argument.\nSee 'docker run --help'.", state)

        val (repo, tag) = splitTag(cmd.image)
        val existingImg = findImage(cmd.image, state)

        val pullLines = if (existingImg == null) buildPullLines(repo, tag) else emptyList()
        val newImages = if (existingImg == null) state.images + makeImage(repo, tag) else state.images

        val cId = genId()
        val cName = cmd.name ?: "${repo.replace("/", "_").replace(".", "_")}_${cId.take(6)}"
        val ipSuffix = state.nextIpSuffix
        val netName = cmd.network ?: "bridge"

        val container = DockerContainer(
            id = cId,
            name = cName,
            imageRef = "$repo:$tag",
            status = ContainerStatus.RUNNING,
            ports = cmd.ports.map { PortBinding(it.hostPort, it.containerPort, it.protocol) },
            envVars = cmd.envVars,
            volumeMounts = cmd.volumes.map { VolumeMount(it.source, it.target) },
            networkName = netName,
            ipAddress = "172.17.0.$ipSuffix",
            createdAtMs = System.currentTimeMillis()
        )

        val updatedNetworks = state.networks.map { net ->
            if (net.name == netName) net.copy(connectedContainerIds = net.connectedContainerIds + cId)
            else net
        }.let { nets ->
            // If network doesn't exist yet, add it (user-created network used in run)
            if (nets.none { it.name == netName }) nets + DockerNetwork(netName, "bridge", "", listOf(cId))
            else nets
        }

        val newState = state.copy(
            containers = state.containers + container,
            images = newImages,
            networks = updatedNetworks,
            nextIpSuffix = ipSuffix + 1
        )

        val statusLabel = if (cmd.detached) "started (detached)" else "started"
        val portsSummary = cmd.ports.joinToString("  ") { "${it.hostPort}:${it.containerPort}" }
        val output = pullLines + buildList {
            add("${OK}Container $statusLabel: $cName")
            add("  Image: $repo:$tag${if (portsSummary.isNotEmpty()) "   Ports: $portsSummary" else ""}")
            add("  Network: $netName   IP: 172.17.0.$ipSuffix")
            add("${ID}  $cId")
        }
        return CommandResult.Success(output, newState, xpEarned = 10)
    }

    // ─── PS ──────────────────────────────────────────────────────────────────

    private fun doPs(cmd: DockerCommand.Ps, state: SimulatorState): CommandResult {
        val visible = if (cmd.all) state.containers
                      else state.containers.filter { it.status == ContainerStatus.RUNNING }
        if (visible.isEmpty()) {
            val msg = if (cmd.all) "No containers found." else "No running containers.  Try: docker ps -a"
            return CommandResult.Success(listOf(msg), state)
        }
        val label = if (cmd.all) "ALL CONTAINERS" else "RUNNING CONTAINERS"
        val lines = buildList {
            add("${H}$label  (${visible.size})")
            visible.forEach { c ->
                val statusIcon = when (c.status) {
                    ContainerStatus.RUNNING -> "●"
                    ContainerStatus.STOPPED -> "○"
                    ContainerStatus.PAUSED  -> "◑"
                    ContainerStatus.CREATED -> "◌"
                }
                val statusTag = c.status.name
                val ports = c.ports.joinToString("  ") { "${it.hostPort}:${it.containerPort}/${it.protocol}" }
                    .ifEmpty { "—" }
                add("${OK}$statusIcon  ${c.name}  [$statusTag]")
                add("  Image   ${c.imageRef}")
                add("  Ports   $ports   Network  ${c.networkName}")
                add("${ID}  ${c.id.take(12)}")
            }
        }
        return CommandResult.Success(lines, state)
    }

    // ─── STOP / START ─────────────────────────────────────────────────────────

    private fun doStop(cmd: DockerCommand.Stop, state: SimulatorState): CommandResult {
        val outputs = mutableListOf<String>()
        var s = state
        var hasError = false
        for (target in cmd.targets) {
            val c = findContainer(target, s)
            if (c == null) {
                outputs += "Error response from daemon: No such container: $target"
                hasError = true
            } else {
                s = s.copy(containers = s.containers.map {
                    if (it.id == c.id) it.copy(status = ContainerStatus.STOPPED) else it
                })
                outputs += "${OK}Stopped: ${c.name}"
            }
        }
        return if (hasError) CommandResult.Error(outputs.joinToString("\n"), s)
        else CommandResult.Success(outputs, s)
    }

    private fun doStart(cmd: DockerCommand.Start, state: SimulatorState): CommandResult {
        val outputs = mutableListOf<String>()
        var s = state
        var hasError = false
        for (target in cmd.targets) {
            val c = findContainer(target, s)
            if (c == null) {
                outputs += "Error response from daemon: No such container: $target"
                hasError = true
            } else {
                s = s.copy(containers = s.containers.map {
                    if (it.id == c.id) it.copy(status = ContainerStatus.RUNNING) else it
                })
                outputs += "${OK}Started: ${c.name}"
            }
        }
        return if (hasError) CommandResult.Error(outputs.joinToString("\n"), s)
        else CommandResult.Success(outputs, s)
    }

    // ─── RM ──────────────────────────────────────────────────────────────────

    private fun doRm(cmd: DockerCommand.Rm, state: SimulatorState): CommandResult {
        var s = state
        val outputs = mutableListOf<String>()
        var hasError = false
        for (target in cmd.targets) {
            val c = findContainer(target, s)
            when {
                c == null -> { outputs += "Error: No such container: $target"; hasError = true }
                c.status == ContainerStatus.RUNNING && !cmd.force ->
                    { outputs += "Error: container ${c.name} is running. Stop it first or use -f"; hasError = true }
                else -> {
                    s = s.copy(containers = s.containers.filter { it.id != c.id })
                    outputs += "${OK}Removed: ${c.name}"
                }
            }
        }
        return if (hasError) CommandResult.Error(outputs.joinToString("\n"), s)
        else CommandResult.Success(outputs, s)
    }

    // ─── PULL ─────────────────────────────────────────────────────────────────

    private fun doPull(cmd: DockerCommand.Pull, state: SimulatorState): CommandResult {
        val existing = findImage("${cmd.image}:${cmd.tag}", state)
        return if (existing != null) {
            CommandResult.Success(listOf(
                "${PL}${cmd.image}:${cmd.tag}: already up to date",
                "${OK}Status: Image is up to date for ${cmd.image}:${cmd.tag}"
            ), state)
        } else {
            val newState = state.copy(images = state.images + makeImage(cmd.image, cmd.tag))
            CommandResult.Success(buildPullLines(cmd.image, cmd.tag), newState, xpEarned = 5)
        }
    }

    // ─── IMAGES ───────────────────────────────────────────────────────────────

    private fun doImages(cmd: DockerCommand.Images, state: SimulatorState): CommandResult {
        val filtered = if (cmd.repository != null)
            state.images.filter { it.repository == cmd.repository }
        else state.images
        if (filtered.isEmpty())
            return CommandResult.Success(listOf("No images found.  Try: docker pull <image>"), state)
        val lines = buildList {
            add("${H}LOCAL IMAGES  (${filtered.size})")
            filtered.forEach { img ->
                val sizeMb = img.sizeBytes / 1_000_000L
                add("${OK}${img.repository}:${img.tag}   ${sizeMb} MB")
                add("${ID}  ${img.id.take(12)}   created 2 hours ago")
            }
        }
        return CommandResult.Success(lines, state)
    }

    // ─── RMI ──────────────────────────────────────────────────────────────────

    private fun doRmi(cmd: DockerCommand.Rmi, state: SimulatorState): CommandResult {
        var s = state
        val outputs = mutableListOf<String>()
        var hasError = false
        for (target in cmd.targets) {
            val img = findImage(target, s)
            if (img == null) {
                outputs += "Error: No such image: $target"
                hasError = true
            } else {
                val inUse = s.containers.any { it.imageRef == "${img.repository}:${img.tag}" && it.status == ContainerStatus.RUNNING }
                if (inUse && !cmd.force) {
                    outputs += "Error: image is being used by running container. Stop it first or use -f"
                    hasError = true
                } else {
                    s = s.copy(images = s.images.filter { it.id != img.id })
                    outputs += "${OK}Removed: ${img.repository}:${img.tag}"
                    outputs += "${ID}  sha256:${img.id}"
                }
            }
        }
        return if (hasError) CommandResult.Error(outputs.joinToString("\n"), s)
        else CommandResult.Success(outputs, s)
    }

    // ─── EXEC ─────────────────────────────────────────────────────────────────

    private fun doExec(cmd: DockerCommand.Exec, state: SimulatorState): CommandResult {
        val c = findContainer(cmd.container, state)
            ?: return CommandResult.Error("Error: No such container: ${cmd.container}", state)
        if (c.status != ContainerStatus.RUNNING)
            return CommandResult.Error("Error: container ${c.name} is not running", state)
        val cmdStr = cmd.command.joinToString(" ")
        val output = simulateExec(c, cmdStr)
        return CommandResult.Success(output, state)
    }

    // ─── LOGS ─────────────────────────────────────────────────────────────────

    private fun doLogs(cmd: DockerCommand.Logs, state: SimulatorState): CommandResult {
        val c = findContainer(cmd.container, state)
            ?: return CommandResult.Error("Error: No such container: ${cmd.container}", state)
        val lines = generateLogs(c, cmd.tail)
        return CommandResult.Success(lines, state)
    }

    // ─── INSPECT ──────────────────────────────────────────────────────────────

    private fun doInspect(cmd: DockerCommand.Inspect, state: SimulatorState): CommandResult {
        val outputs = mutableListOf<String>()
        for (target in cmd.targets) {
            val c = findContainer(target, state)
            val img = if (c == null) findImage(target, state) else null
            val net = if (c == null && img == null) state.networks.find { it.name == target } else null
            when {
                c != null -> outputs += inspectContainer(c)
                img != null -> outputs += inspectImage(img)
                net != null -> outputs += inspectNetwork(net)
                else -> outputs += "Error: No such object: $target"
            }
        }
        return CommandResult.Success(outputs, state)
    }

    // ─── BUILD ────────────────────────────────────────────────────────────────

    private fun doBuild(cmd: DockerCommand.Build, state: SimulatorState): CommandResult {
        val imgId = genId()
        val (repo, tag) = if (cmd.tag != null) splitTag(cmd.tag) else "app" to "latest"
        val steps = listOf(
            "  Step 1/4 → FROM ubuntu:22.04",
            "${ID}    cached: ${genId().take(12)}",
            "  Step 2/4 → WORKDIR /app",
            "${ID}    ${genId().take(12)}",
            "  Step 3/4 → COPY . .",
            "${ID}    ${genId().take(12)}",
            "  Step 4/4 → CMD [\"./start.sh\"]",
            "${ID}    ${genId().take(12)}",
            "${OK}Built: $imgId"
        )
        val tagLine = if (cmd.tag != null) listOf("${OK}Tagged: $repo:$tag") else emptyList()
        val newImg = makeImage(repo, tag, imgId)
        val newState = state.copy(images = state.images + newImg)
        return CommandResult.Success(steps + tagLine, newState, xpEarned = 15)
    }

    // ─── TAG ──────────────────────────────────────────────────────────────────

    private fun doTag(cmd: DockerCommand.Tag, state: SimulatorState): CommandResult {
        val srcImg = findImage(cmd.source, state)
            ?: return CommandResult.Error("Error: No such image: ${cmd.source}", state)
        val (tRepo, tTag) = splitTag(cmd.target)
        val tagged = srcImg.copy(repository = tRepo, tag = tTag)
        return CommandResult.Success(emptyList(), state.copy(images = state.images + tagged))
    }

    // ─── VOLUME ───────────────────────────────────────────────────────────────

    private fun doVolumeCreate(cmd: DockerCommand.VolumeCreate, state: SimulatorState): CommandResult {
        if (state.volumes.any { it.name == cmd.name })
            return CommandResult.Success(listOf("${OK}Volume already exists: ${cmd.name}"), state)
        val vol = DockerVolume(cmd.name)
        return CommandResult.Success(
            listOf("${OK}Created volume: ${cmd.name}", "  Driver: local"),
            state.copy(volumes = state.volumes + vol), xpEarned = 5
        )
    }

    private fun doVolumeLs(state: SimulatorState): CommandResult {
        if (state.volumes.isEmpty())
            return CommandResult.Success(listOf("No volumes.  Create one: docker volume create <name>"), state)
        val lines = buildList {
            add("${H}VOLUMES  (${state.volumes.size})")
            state.volumes.forEach { vol ->
                add("${OK}${vol.name}   driver: ${vol.driver}")
                add("  ${vol.mountpoint}")
            }
        }
        return CommandResult.Success(lines, state)
    }

    private fun doVolumeRm(cmd: DockerCommand.VolumeRm, state: SimulatorState): CommandResult {
        var s = state
        val out = mutableListOf<String>()
        var hasError = false
        for (name in cmd.names) {
            val inUse = s.containers.any { c -> c.volumeMounts.any { it.volumeName == name } }
            if (inUse) { out += "Error: volume $name is in use by a container"; hasError = true }
            else {
                s = s.copy(volumes = s.volumes.filter { it.name != name })
                out += "${OK}Removed volume: $name"
            }
        }
        return if (hasError) CommandResult.Error(out.joinToString("\n"), s)
        else CommandResult.Success(out, s)
    }

    private fun doVolumeInspect(cmd: DockerCommand.VolumeInspect, state: SimulatorState): CommandResult {
        val vol = state.volumes.find { it.name == cmd.name }
            ?: return CommandResult.Error("Error: No such volume: ${cmd.name}", state)
        return CommandResult.Success(listOf(
            "[",
            "  {",
            "    \"Name\": \"${vol.name}\",",
            "    \"Driver\": \"${vol.driver}\",",
            "    \"Mountpoint\": \"${vol.mountpoint}\",",
            "    \"Labels\": {},",
            "    \"Scope\": \"local\"",
            "  }",
            "]"
        ), state)
    }

    // ─── NETWORK ──────────────────────────────────────────────────────────────

    private fun doNetworkCreate(cmd: DockerCommand.NetworkCreate, state: SimulatorState): CommandResult {
        if (state.networks.any { it.name == cmd.name })
            return CommandResult.Error("Error response from daemon: network with name ${cmd.name} already exists", state)
        val subnet = "192.168.${state.networks.size}.0/24"
        val net = DockerNetwork(cmd.name, cmd.driver, subnet)
        val netId = genId() + genId().take(4)
        val newState = state.copy(networks = state.networks + net)
        return CommandResult.Success(
            listOf(
                "${OK}Created network: ${cmd.name}   driver: ${cmd.driver}",
                "  Subnet: $subnet",
                "${ID}  $netId"
            ),
            newState, xpEarned = 5
        )
    }

    private fun doNetworkLs(state: SimulatorState): CommandResult {
        val lines = buildList {
            add("${H}NETWORKS  (${state.networks.size})")
            state.networks.forEach { net ->
                val connCount = net.connectedContainerIds.size
                val extra = if (connCount > 0) "   $connCount container(s)" else ""
                add("${OK}${net.name}   driver: ${net.driver}$extra")
                if (net.subnet.isNotEmpty()) add("  subnet: ${net.subnet}")
            }
        }
        return CommandResult.Success(lines, state)
    }

    private fun doNetworkConnect(cmd: DockerCommand.NetworkConnect, state: SimulatorState): CommandResult {
        val net = state.networks.find { it.name == cmd.network }
            ?: return CommandResult.Error("Error: network ${cmd.network} not found", state)
        val c = findContainer(cmd.container, state)
            ?: return CommandResult.Error("Error: container ${cmd.container} not found", state)
        if (c.id in net.connectedContainerIds)
            return CommandResult.Error("Error: endpoint with name ${c.name} already exists in network ${cmd.network}", state)
        val updatedNetworks = state.networks.map {
            if (it.name == cmd.network) it.copy(connectedContainerIds = it.connectedContainerIds + c.id)
            else it
        }
        return CommandResult.Success(
            listOf("${OK}Connected ${c.name} to network ${cmd.network}"),
            state.copy(networks = updatedNetworks), xpEarned = 5
        )
    }

    private fun doNetworkDisconnect(cmd: DockerCommand.NetworkDisconnect, state: SimulatorState): CommandResult {
        val c = findContainer(cmd.container, state)
            ?: return CommandResult.Error("Error: container ${cmd.container} not found", state)
        val updatedNetworks = state.networks.map {
            if (it.name == cmd.network) it.copy(connectedContainerIds = it.connectedContainerIds - c.id)
            else it
        }
        return CommandResult.Success(
            listOf("${OK}Disconnected ${c.name} from network ${cmd.network}"),
            state.copy(networks = updatedNetworks)
        )
    }

    private fun doNetworkRm(cmd: DockerCommand.NetworkRm, state: SimulatorState): CommandResult {
        var s = state
        val out = mutableListOf<String>()
        var hasError = false
        for (name in cmd.names) {
            val inUse = s.containers.any { it.networkName == name }
            if (inUse) { out += "Error: network $name has active endpoints"; hasError = true }
            else {
                s = s.copy(networks = s.networks.filter { it.name != name })
                out += "${OK}Removed network: $name"
            }
        }
        return if (hasError) CommandResult.Error(out.joinToString("\n"), s)
        else CommandResult.Success(out, s)
    }

    private fun doNetworkInspect(cmd: DockerCommand.NetworkInspect, state: SimulatorState): CommandResult {
        val net = state.networks.find { it.name == cmd.name }
            ?: return CommandResult.Error("Error: No such network: ${cmd.name}", state)
        val containers = state.containers.filter { it.id in net.connectedContainerIds }
        val cJson = containers.joinToString(",\n") { c ->
            "      \"${c.id}\": { \"Name\": \"${c.name}\", \"IPv4Address\": \"${c.ipAddress}\" }"
        }
        return CommandResult.Success(listOf(
            "[{ \"Name\": \"${net.name}\", \"Driver\": \"${net.driver}\", \"Subnet\": \"${net.subnet}\",",
            "   \"Containers\": {",
            cJson,
            "  }",
            "}]"
        ), state)
    }

    // ─── PRUNE ────────────────────────────────────────────────────────────────

    private fun doContainerPrune(state: SimulatorState): CommandResult {
        val stopped = state.containers.filter { it.status != ContainerStatus.RUNNING }
        if (stopped.isEmpty()) return CommandResult.Success(
            listOf("${OK}No stopped containers to remove.", "  Total reclaimed space: 0B"), state
        )
        val newState = state.copy(containers = state.containers.filter { it.status == ContainerStatus.RUNNING })
        val out = buildList {
            add("${H}DELETED CONTAINERS  (${stopped.size})")
            stopped.forEach { c ->
                add("${OK}Removed: ${c.name}")
                add("${ID}  ${c.id.take(12)}")
            }
            add("  Total reclaimed space: ${stopped.size * 12} MB")
        }
        return CommandResult.Success(out, newState, xpEarned = 5)
    }

    private fun doImagePrune(cmd: DockerCommand.ImagePrune, state: SimulatorState): CommandResult {
        val usedImages = state.containers.map { it.imageRef }.toSet()
        val dangling = if (cmd.all) state.images.filter { "${it.repository}:${it.tag}" !in usedImages }
                       else state.images.filter { it.tag == "<none>" }
        if (dangling.isEmpty()) return CommandResult.Success(
            listOf("${OK}No unused images to remove.", "  Total reclaimed space: 0B"), state
        )
        val newState = state.copy(images = state.images.filter { it !in dangling })
        val out = buildList {
            add("${H}DELETED IMAGES  (${dangling.size})")
            dangling.forEach { img ->
                add("${OK}Removed: ${img.repository}:${img.tag}")
                add("${ID}  sha256:${img.id.take(12)}")
            }
            add("  Total reclaimed space: ${dangling.size * 50} MB")
        }
        return CommandResult.Success(out, newState, xpEarned = 5)
    }

    private fun doSystemPrune(cmd: DockerCommand.SystemPrune, state: SimulatorState): CommandResult {
        val stopped = state.containers.filter { it.status != ContainerStatus.RUNNING }
        val usedImages = state.containers.filter { it.status == ContainerStatus.RUNNING }.map { it.imageRef }.toSet()
        val unusedImages = if (cmd.all) state.images.filter { "${it.repository}:${it.tag}" !in usedImages }
                           else state.images.filter { it.tag == "<none>" }
        val customNets = state.networks.filter { it.name !in listOf("bridge", "host", "none") }
        val unusedNets = customNets.filter { net -> state.containers.none { it.networkName == net.name } }
        val unusedVols = if (cmd.withVolumes) state.volumes.filter { vol ->
            state.containers.none { c -> c.volumeMounts.any { it.volumeName == vol.name } }
        } else emptyList()

        val newContainers = state.containers.filter { it.status == ContainerStatus.RUNNING }
        val newImages = state.images - unusedImages.toSet()
        val newNets = state.networks - unusedNets.toSet()
        val newVols = state.volumes - unusedVols.toSet()
        val reclaimedMb = stopped.size * 12 + unusedImages.size * 50

        val out = buildList {
            add("${H}SYSTEM PRUNE")
            add("  Removing stopped containers, unused networks, dangling images${if (cmd.withVolumes) ", volumes" else ""}")
            if (stopped.isNotEmpty()) {
                add("${H}CONTAINERS REMOVED  (${stopped.size})")
                stopped.forEach { c -> add("${OK}Removed: ${c.name}") }
            }
            if (unusedImages.isNotEmpty()) {
                add("${H}IMAGES REMOVED  (${unusedImages.size})")
                unusedImages.forEach { img -> add("${OK}Removed: ${img.repository}:${img.tag}") }
            }
            if (unusedNets.isNotEmpty()) {
                add("${H}NETWORKS REMOVED  (${unusedNets.size})")
                unusedNets.forEach { net -> add("${OK}Removed: ${net.name}") }
            }
            if (unusedVols.isNotEmpty()) {
                add("${H}VOLUMES REMOVED  (${unusedVols.size})")
                unusedVols.forEach { vol -> add("${OK}Removed: ${vol.name}") }
            }
            add("${OK}Total reclaimed space: $reclaimedMb MB")
        }
        return CommandResult.Success(out, state.copy(containers = newContainers, images = newImages, networks = newNets, volumes = newVols), xpEarned = 10)
    }

    // ─── COMPOSE ──────────────────────────────────────────────────────────────

    private fun doComposeUp(cmd: DockerCommand.ComposeUp, state: SimulatorState): CommandResult {
        val stackName = "app"
        val services = listOf(
            ComposeService("web", "nginx", 1),
            ComposeService("db", "redis", 1),
        )
        val newContainers = services.flatMap { svc ->
            (1..svc.scale).map { i ->
                val cId = genId()
                DockerContainer(
                    id = cId, name = "${stackName}_${svc.name}_$i", imageRef = "${svc.image}:latest",
                    status = ContainerStatus.RUNNING, networkName = "${stackName}_default",
                    ipAddress = "172.20.0.${state.nextIpSuffix + i}"
                )
            }
        }
        val stackNet = DockerNetwork("${stackName}_default", "bridge", "172.20.0.0/24",
            newContainers.map { it.id })
        val stack = ComposeStack(stackName, services.mapIndexed { i, svc ->
            svc.copy(containerIds = listOf(newContainers[i].id))
        })
        val existingStack = state.composeStacks.find { it.name == stackName }
        val newState = state.copy(
            containers = if (existingStack == null) state.containers + newContainers else state.containers,
            networks = if (state.networks.none { it.name == stackNet.name }) state.networks + stackNet else state.networks,
            composeStacks = if (existingStack == null) state.composeStacks + stack
                            else state.composeStacks.map { if (it.name == stackName) stack else it },
            nextIpSuffix = state.nextIpSuffix + newContainers.size
        )
        val out = buildList {
            add("${H}COMPOSE UP  —  stack: $stackName")
            services.forEach { svc ->
                add("${PL}Pulling ${svc.image}:latest ...")
                add("${OK}Created: ${stackName}_${svc.name}_1")
            }
            add("${OK}Network ${stackName}_default created")
            add("  ${services.size} service(s) running")
        }
        return CommandResult.Success(out, newState, xpEarned = 20)
    }

    private fun doComposeDown(cmd: DockerCommand.ComposeDown, state: SimulatorState): CommandResult {
        val stack = state.composeStacks.firstOrNull()
        val ids = stack?.services?.flatMap { it.containerIds } ?: emptyList()
        val names = state.containers.filter { it.id in ids }.map { it.name }
        val newContainers = state.containers.filter { it.id !in ids }
        val newStacks = state.composeStacks.filter { it.name != stack?.name }
        val out = buildList {
            if (names.isEmpty()) {
                add("No compose stack to stop.")
            } else {
                add("${H}COMPOSE DOWN")
                names.forEach { add("${OK}Stopped & removed: $it") }
                add("  Network ${stack?.name}_default removed")
            }
        }
        return CommandResult.Success(out, state.copy(containers = newContainers, composeStacks = newStacks))
    }

    private fun doComposePs(state: SimulatorState): CommandResult {
        val stack = state.composeStacks.firstOrNull()
            ?: return CommandResult.Success(listOf("No compose stack running.  Try: docker-compose up -d"), state)
        val lines = buildList {
            add("${H}COMPOSE SERVICES  —  stack: ${stack.name}  (${stack.services.size})")
            stack.services.forEach { svc ->
                val cName = "${stack.name}_${svc.name}_1"
                add("${OK}●  $cName  [Up]")
                add("  Image: ${svc.image}:latest   Scale: ${svc.scale}")
            }
        }
        return CommandResult.Success(lines, state)
    }

    private fun doComposeLogs(cmd: DockerCommand.ComposeLogs, state: SimulatorState): CommandResult {
        val stack = state.composeStacks.firstOrNull()
            ?: return CommandResult.Success(listOf("No compose stack running"), state)
        val lines = buildList {
            add("${H}COMPOSE LOGS  —  stack: ${stack.name}")
            stack.services.forEach { svc ->
                add("${PL}${svc.name}_1  | ${logTs()} [INFO] Service ${svc.name} started")
                add("${PL}${svc.name}_1  | ${logTs()} [INFO] Listening on port 80")
                add("${PL}${svc.name}_1  | ${logTs()} [INFO] Ready to accept connections")
            }
        }
        return CommandResult.Success(lines, state)
    }

    private fun doComposeScale(cmd: DockerCommand.ComposeScale, state: SimulatorState): CommandResult {
        val stack = state.composeStacks.firstOrNull()
            ?: return CommandResult.Error("No compose stack running. Run 'docker-compose up -d' first.", state)
        val svc = stack.services.find { it.name == cmd.service }
            ?: return CommandResult.Error("No service '${cmd.service}' in compose stack", state)

        val currentCount = svc.containerIds.size
        val newContainers = if (cmd.replicas > currentCount) {
            (currentCount + 1..cmd.replicas).map { i ->
                DockerContainer(
                    id = genId(), name = "app_${cmd.service}_$i",
                    imageRef = "${svc.image}:latest", status = ContainerStatus.RUNNING,
                    networkName = "app_default", ipAddress = "172.20.0.${state.nextIpSuffix + i}"
                )
            }
        } else emptyList()

        val updatedSvc = svc.copy(
            scale = cmd.replicas,
            containerIds = svc.containerIds.take(cmd.replicas) + newContainers.map { it.id }
        )
        val updatedStack = stack.copy(services = stack.services.map { if (it.name == cmd.service) updatedSvc else it })
        val out = if (cmd.replicas > currentCount)
            newContainers.map { "Creating app_${cmd.service}_${newContainers.indexOf(it) + currentCount + 1} ... done" }
        else listOf("Stopping and removing containers...")

        return CommandResult.Success(out, state.copy(
            containers = state.containers + newContainers,
            composeStacks = state.composeStacks.map { if (it.name == stack.name) updatedStack else it },
            nextIpSuffix = state.nextIpSuffix + newContainers.size
        ), xpEarned = 10)
    }

    // ─── INFO / VERSION ───────────────────────────────────────────────────────

    private fun doInfo(state: SimulatorState) = CommandResult.Success(buildList {
        add("${H}DOCKER INFO  —  StackForge Engine")
        add("${OK}Containers: ${state.containers.size}")
        add("  Running  ${state.containers.count { it.status == ContainerStatus.RUNNING }}")
        add("  Stopped  ${state.containers.count { it.status == ContainerStatus.STOPPED }}")
        add("${OK}Images:    ${state.images.size}")
        add("${OK}Volumes:   ${state.volumes.size}")
        add("${OK}Networks:  ${state.networks.size}")
        add("${H}ENGINE")
        add("  Server Version  28.0.1")
        add("  Storage Driver  overlay2")
        add("  Kernel Version  6.8.0-generic")
        add("  OS              Ubuntu 24.04 LTS")
    }, state)

    private fun doVersion(state: SimulatorState) = CommandResult.Success(buildList {
        add("${H}DOCKER VERSION")
        add("${OK}Client  28.0.1  (build b9d08b6)")
        add("${OK}Engine  28.0.1")
        add("${OK}Compose 2.34.0")
    }, state)

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private fun genId(): String {
        val chars = "0123456789abcdef"
        return (1..12).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun splitTag(s: String): Pair<String, String> {
        val lastColon = s.lastIndexOf(':')
        return if (lastColon > 0 && '/' !in s.substring(lastColon)) s.substring(0, lastColon) to s.substring(lastColon + 1)
        else s to "latest"
    }

    private fun findContainer(nameOrId: String, state: SimulatorState): DockerContainer? =
        state.containers.find { it.name == nameOrId }
            ?: state.containers.find { it.id.startsWith(nameOrId) }

    private fun findImage(nameOrTag: String, state: SimulatorState): DockerImage? {
        val (repo, tag) = splitTag(nameOrTag)
        return state.images.find { it.repository == repo && it.tag == tag }
            ?: state.images.find { it.repository == nameOrTag }
    }

    private fun makeImage(repo: String, tag: String, id: String = genId()) = DockerImage(
        id = id, repository = repo, tag = tag,
        sizeBytes = (10..200).random().toLong() * 1_000_000L,
        createdAtMs = System.currentTimeMillis()
    )

    private fun buildPullLines(repo: String, tag: String): List<String> = listOf(
        "${PL}Pulling $repo:$tag from registry ...",
        "${PL}${genId().take(8)}: Pull complete",
        "${PL}${genId().take(8)}: Pull complete",
        "${PL}${genId().take(8)}: Pull complete",
        "${PL}Digest: sha256:${genId()}${genId()}",
        "${OK}Downloaded: $repo:$tag"
    )

    private fun simulateExec(c: DockerContainer, cmd: String): List<String> {
        return when {
            cmd.isBlank() || cmd == "bash" || cmd == "sh" || cmd == "/bin/bash" || cmd == "/bin/sh" ->
                listOf("[trainee@${c.name}:/]$ (interactive shell — type 'exit' to leave)")
            cmd.startsWith("ls") -> listOf("bin  dev  etc  home  lib  proc  root  sys  tmp  usr  var")
            cmd.startsWith("cat /etc/os-release") -> listOf("NAME=\"Ubuntu\"", "VERSION=\"24.04 LTS (Noble Numbat)\"")
            cmd.startsWith("ps") -> listOf("PID   USER     COMMAND", "  1   root     ${c.imageRef.substringBefore(":")}", " 42   root     ps")
            cmd.startsWith("env") -> c.envVars.map { (k, v) -> "$k=$v" }.ifEmpty { listOf("(no env vars)") }
            else -> listOf("$ $cmd", "[command executed in ${c.name}]")
        }
    }

    private fun generateLogs(c: DockerContainer, tail: Int?): List<String> {
        val image = c.imageRef.substringBefore(":")
        val allLogs = when {
            image.contains("nginx") || image.contains("web") -> listOf(
                "${logTs()} [notice] nginx: master process",
                "${logTs()} [notice] start worker processes",
                "${logTs()} [info] 172.17.0.1 - GET / HTTP/1.1 200",
                "${logTs()} [info] 172.17.0.1 - GET /health HTTP/1.1 200"
            )
            image.contains("redis") || image.contains("db") -> listOf(
                "${logTs()} * oO0OoO0OoO0Oo Redis is starting oO0OoO0OoO0Oo",
                "${logTs()} * Redis version=7.2.0",
                "${logTs()} * Ready to accept connections"
            )
            image.contains("postgres") -> listOf(
                "${logTs()} LOG: database system is ready to accept connections",
                "${logTs()} LOG: autovacuum launcher started"
            )
            else -> listOf(
                "${logTs()} [INFO] Service ${c.name} started",
                "${logTs()} [INFO] Listening on port 80",
                "${logTs()} [INFO] Health check passed",
                "${logTs()} [INFO] Ready to accept connections"
            )
        }
        return if (tail != null) allLogs.takeLast(tail) else allLogs
    }

    private fun logTs(): String {
        val h = (8..16).random().toString().padStart(2, '0')
        val m = (10..59).random().toString().padStart(2, '0')
        val s = (10..59).random().toString().padStart(2, '0')
        return "2024-11-15T$h:$m:${s}Z"
    }

    private fun inspectContainer(c: DockerContainer): List<String> = listOf(
        "[",
        "  {",
        "    \"Id\": \"${c.id}\",",
        "    \"Name\": \"/${c.name}\",",
        "    \"State\": { \"Status\": \"${c.status.name.lowercase()}\", \"Running\": ${c.status == ContainerStatus.RUNNING} },",
        "    \"Image\": \"${c.imageRef}\",",
        "    \"NetworkSettings\": { \"IPAddress\": \"${c.ipAddress}\" },",
        "    \"Mounts\": [${c.volumeMounts.joinToString { "{ \"Source\": \"${it.volumeName}\", \"Destination\": \"${it.mountPath}\" }" }}],",
        "    \"Config\": { \"Env\": [${c.envVars.map { (k, v) -> "\"$k=$v\"" }.joinToString()}] }",
        "  }",
        "]"
    )

    private fun inspectImage(img: DockerImage): List<String> = listOf(
        "[",
        "  {",
        "    \"Id\": \"sha256:${img.id}\",",
        "    \"RepoTags\": [\"${img.repository}:${img.tag}\"],",
        "    \"Size\": ${img.sizeBytes}",
        "  }",
        "]"
    )

    private fun inspectNetwork(net: DockerNetwork): List<String> = listOf(
        "[",
        "  {",
        "    \"Name\": \"${net.name}\",",
        "    \"Driver\": \"${net.driver}\",",
        "    \"IPAM\": { \"Config\": [{ \"Subnet\": \"${net.subnet}\" }] }",
        "  }",
        "]"
    )

    private val HELP_LINES = listOf(
        "",
        "Docker Engine — Learn Docker",
        "",
        "Usage:  docker [OPTIONS] COMMAND",
        "",
        "Management Commands:",
        "  container   Manage containers",
        "  image       Manage images",
        "  network     Manage networks",
        "  system      Manage Docker",
        "  volume      Manage volumes",
        "",
        "Commands:",
        "  build       Build image from Dockerfile",
        "  exec        Run command in container",
        "  images      List images",
        "  inspect     Inspect resources",
        "  logs        Fetch container logs",
        "  ps          List containers",
        "  pull        Download image",
        "  rm          Remove containers",
        "  rmi         Remove images",
        "  run         Create & start container",
        "  start       Start stopped container",
        "  stop        Stop running container",
        "  tag         Tag an image",
        "",
        "  clear       Clear terminal",
        ""
    )
}
