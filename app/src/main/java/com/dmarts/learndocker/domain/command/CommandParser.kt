package com.dmarts.learndocker.domain.command

class CommandParser {

    fun parse(input: String): ParseResult {
        val tokens = tokenize(input.trim())
        if (tokens.isEmpty()) return ParseResult.Failure("Empty command")

        return when (tokens[0].lowercase()) {
            "docker" -> {
                if (tokens.size < 2) return ParseResult.Failure("Usage: docker COMMAND [OPTIONS]")
                parseDockerSub(tokens[1].lowercase(), tokens.drop(2))
            }
            "docker-compose" -> {
                if (tokens.size < 2) return ParseResult.Failure("Usage: docker-compose COMMAND")
                parseComposeSub(tokens[1].lowercase(), tokens.drop(2))
            }
            "clear", "cls" -> ParseResult.Success(DockerCommand.Clear)
            "help" -> ParseResult.Success(DockerCommand.Help)
            else -> ParseResult.Failure("'${tokens[0]}' is not recognized. Type 'docker help' for commands.")
        }
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inSingle = false
        var inDouble = false
        for (ch in input) {
            when {
                ch == '\'' && !inDouble -> inSingle = !inSingle
                ch == '"' && !inSingle -> inDouble = !inDouble
                ch == ' ' && !inSingle && !inDouble -> {
                    if (current.isNotEmpty()) { tokens += current.toString(); current.clear() }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) tokens += current.toString()
        return tokens
    }

    private fun parseDockerSub(sub: String, args: List<String>): ParseResult = when (sub) {
        "run" -> ParseResult.Success(parseRun(args))
        "ps" -> ParseResult.Success(DockerCommand.Ps(all = "-a" in args || "--all" in args))
        "stop" -> {
            val targets = args.filter { !it.startsWith("-") }
            if (targets.isEmpty()) ParseResult.Failure("'docker stop' requires at least one container name/id")
            else ParseResult.Success(DockerCommand.Stop(targets))
        }
        "start" -> {
            val targets = args.filter { !it.startsWith("-") }
            if (targets.isEmpty()) ParseResult.Failure("'docker start' requires at least one container name/id")
            else ParseResult.Success(DockerCommand.Start(targets))
        }
        "rm" -> {
            val force = "-f" in args || "--force" in args
            val targets = args.filter { !it.startsWith("-") }
            if (targets.isEmpty()) ParseResult.Failure("'docker rm' requires at least one container name/id")
            else ParseResult.Success(DockerCommand.Rm(targets, force))
        }
        "pull" -> {
            val target = args.firstOrNull { !it.startsWith("-") }
                ?: return ParseResult.Failure("'docker pull' requires an image name")
            val (repo, tag) = splitImageTag(target)
            ParseResult.Success(DockerCommand.Pull(repo, tag))
        }
        "images" -> ParseResult.Success(DockerCommand.Images(args.firstOrNull { !it.startsWith("-") }))
        "rmi" -> {
            val force = "-f" in args || "--force" in args
            val targets = args.filter { !it.startsWith("-") }
            if (targets.isEmpty()) ParseResult.Failure("'docker rmi' requires at least one image")
            else ParseResult.Success(DockerCommand.Rmi(targets, force))
        }
        "exec" -> parseExec(args)
        "logs" -> parseLogs(args)
        "inspect" -> {
            val targets = args.filter { !it.startsWith("-") }
            if (targets.isEmpty()) ParseResult.Failure("'docker inspect' requires a target")
            else ParseResult.Success(DockerCommand.Inspect(targets))
        }
        "build" -> ParseResult.Success(parseBuild(args))
        "tag" -> {
            val positional = args.filter { !it.startsWith("-") }
            if (positional.size < 2) ParseResult.Failure("Usage: docker tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]")
            else ParseResult.Success(DockerCommand.Tag(positional[0], positional[1]))
        }
        "volume" -> parseVolumeCmd(args)
        "network" -> parseNetworkCmd(args)
        "container" -> parseContainerCmd(args)
        "image" -> parseImageCmd(args)
        "system" -> parseSystemCmd(args)
        "compose" -> parseComposeSub(args.firstOrNull()?.lowercase() ?: "", args.drop(1))
        "info" -> ParseResult.Success(DockerCommand.Info)
        "version", "--version", "-v" -> ParseResult.Success(DockerCommand.Version)
        "help", "--help", "-h" -> ParseResult.Success(DockerCommand.Help)
        else -> ParseResult.Failure("'docker $sub' is not a known command. Try 'docker help'")
    }

    private fun parseRun(args: List<String>): DockerCommand.Run {
        var i = 0
        var name: String? = null
        var detached = false
        var rm = false
        var interactive = false
        val ports = mutableListOf<PortMapping>()
        val envVars = mutableMapOf<String, String>()
        val volumes = mutableListOf<VolumeMapping>()
        var network: String? = null
        var image = ""
        val command = mutableListOf<String>()

        while (i < args.size) {
            val arg = args[i]
            when {
                arg == "--name" -> { name = args.getOrNull(++i); i++ }
                arg.startsWith("--name=") -> { name = arg.substringAfter('='); i++ }
                arg == "-d" || arg == "--detach" -> { detached = true; i++ }
                arg == "--rm" -> { rm = true; i++ }
                arg == "-i" || arg == "--interactive" -> { interactive = true; i++ }
                arg == "-t" || arg == "--tty" -> { i++ }
                arg == "-it" || arg == "-ti" -> { interactive = true; i++ }
                arg == "-dit" || arg == "-dti" || arg == "-idt" -> { detached = true; interactive = true; i++ }
                // Combined -d with other flags like -dp
                arg.startsWith("-d") && arg.length == 2 -> { detached = true; i++ }
                arg == "-p" || arg == "--publish" -> {
                    args.getOrNull(++i)?.let { parsePort(it)?.let { pm -> ports.add(pm) } }
                    i++
                }
                arg.startsWith("-p=") -> { parsePort(arg.substringAfter('='))?.let { ports.add(it) }; i++ }
                arg == "-e" || arg == "--env" -> {
                    args.getOrNull(++i)?.let { parseEnv(it)?.let { (k, v) -> envVars[k] = v } }
                    i++
                }
                arg.startsWith("-e") && arg.length > 2 -> {
                    parseEnv(arg.drop(2))?.let { (k, v) -> envVars[k] = v }
                    i++
                }
                arg.startsWith("--env=") -> { parseEnv(arg.substringAfter('='))?.let { (k, v) -> envVars[k] = v }; i++ }
                arg == "--env-file" -> { i += 2 }
                arg == "-v" || arg == "--volume" -> {
                    args.getOrNull(++i)?.let { parseVolume(it)?.let { vm -> volumes.add(vm) } }
                    i++
                }
                arg.startsWith("--volume=") -> { parseVolume(arg.substringAfter('='))?.let { volumes.add(it) }; i++ }
                arg == "--network" || arg == "--net" -> { network = args.getOrNull(++i); i++ }
                arg.startsWith("--network=") -> { network = arg.substringAfter('='); i++ }
                arg.startsWith("-") -> { i++ }
                else -> {
                    image = arg; i++
                    while (i < args.size) { command.add(args[i]); i++ }
                }
            }
        }
        return DockerCommand.Run(image, name, detached, ports, envVars, volumes, network, rm, interactive, command)
    }

    private fun parseExec(args: List<String>): ParseResult {
        var i = 0
        var interactive = false
        var container = ""
        val cmd = mutableListOf<String>()
        while (i < args.size) {
            val arg = args[i]
            when {
                arg == "-i" || arg == "--interactive" || arg == "-it" || arg == "-ti" -> { interactive = true; i++ }
                arg == "-t" || arg == "--tty" -> { i++ }
                arg.startsWith("-") -> { i++ }
                else -> {
                    container = arg; i++
                    while (i < args.size) { cmd.add(args[i]); i++ }
                }
            }
        }
        return if (container.isEmpty()) ParseResult.Failure("Usage: docker exec [OPTIONS] CONTAINER COMMAND")
        else ParseResult.Success(DockerCommand.Exec(container, interactive, cmd))
    }

    private fun parseLogs(args: List<String>): ParseResult {
        var follow = false
        var tail: Int? = null
        var container = ""
        var i = 0
        while (i < args.size) {
            when (val arg = args[i]) {
                "-f", "--follow" -> { follow = true; i++ }
                "--tail" -> { tail = args.getOrNull(++i)?.toIntOrNull(); i++ }
                else -> if (arg.startsWith("--tail=")) {
                    tail = arg.substringAfter('=').toIntOrNull(); i++
                } else if (!arg.startsWith("-")) {
                    container = arg; i++
                } else i++
            }
        }
        return if (container.isEmpty()) ParseResult.Failure("Usage: docker logs [OPTIONS] CONTAINER")
        else ParseResult.Success(DockerCommand.Logs(container, follow, tail))
    }

    private fun parseBuild(args: List<String>): DockerCommand.Build {
        var tag: String? = null
        var context = "."
        var i = 0
        while (i < args.size) {
            when (val arg = args[i]) {
                "-t", "--tag" -> { tag = args.getOrNull(++i); i++ }
                else -> if (arg.startsWith("-t=") || arg.startsWith("--tag=")) {
                    tag = arg.substringAfter('='); i++
                } else if (!arg.startsWith("-")) {
                    context = arg; i++
                } else i++
            }
        }
        return DockerCommand.Build(tag, context)
    }

    private fun parseVolumeCmd(args: List<String>): ParseResult {
        val sub = args.firstOrNull()?.lowercase() ?: return ParseResult.Failure("Usage: docker volume COMMAND")
        return when (sub) {
            "create" -> {
                val name = args.getOrNull(1)
                    ?: return ParseResult.Failure("Usage: docker volume create [OPTIONS] [VOLUME]")
                ParseResult.Success(DockerCommand.VolumeCreate(name))
            }
            "ls", "list" -> ParseResult.Success(DockerCommand.VolumeLs)
            "rm", "remove" -> {
                val names = args.drop(1).filter { !it.startsWith("-") }
                if (names.isEmpty()) ParseResult.Failure("Usage: docker volume rm VOLUME [VOLUME...]")
                else ParseResult.Success(DockerCommand.VolumeRm(names))
            }
            "inspect" -> {
                val name = args.getOrNull(1) ?: return ParseResult.Failure("Usage: docker volume inspect VOLUME")
                ParseResult.Success(DockerCommand.VolumeInspect(name))
            }
            else -> ParseResult.Failure("'docker volume $sub' is not supported")
        }
    }

    private fun parseNetworkCmd(args: List<String>): ParseResult {
        val sub = args.firstOrNull()?.lowercase() ?: return ParseResult.Failure("Usage: docker network COMMAND")
        return when (sub) {
            "create" -> {
                var driver = "bridge"
                val positional = mutableListOf<String>()
                var i = 1
                while (i < args.size) {
                    when (val a = args[i]) {
                        "--driver", "-d" -> { driver = args.getOrNull(++i) ?: "bridge"; i++ }
                        else -> if (!a.startsWith("-")) { positional.add(a); i++ } else i++
                    }
                }
                val name = positional.firstOrNull()
                    ?: return ParseResult.Failure("Usage: docker network create [OPTIONS] NETWORK")
                ParseResult.Success(DockerCommand.NetworkCreate(name, driver))
            }
            "ls", "list" -> ParseResult.Success(DockerCommand.NetworkLs)
            "connect" -> {
                val positional = args.drop(1).filter { !it.startsWith("-") }
                if (positional.size < 2) ParseResult.Failure("Usage: docker network connect NETWORK CONTAINER")
                else ParseResult.Success(DockerCommand.NetworkConnect(positional[0], positional[1]))
            }
            "disconnect" -> {
                val positional = args.drop(1).filter { !it.startsWith("-") }
                if (positional.size < 2) ParseResult.Failure("Usage: docker network disconnect NETWORK CONTAINER")
                else ParseResult.Success(DockerCommand.NetworkDisconnect(positional[0], positional[1]))
            }
            "rm", "remove" -> {
                val names = args.drop(1).filter { !it.startsWith("-") }
                if (names.isEmpty()) ParseResult.Failure("Usage: docker network rm NETWORK [NETWORK...]")
                else ParseResult.Success(DockerCommand.NetworkRm(names))
            }
            "inspect" -> {
                val name = args.getOrNull(1) ?: return ParseResult.Failure("Usage: docker network inspect NETWORK")
                ParseResult.Success(DockerCommand.NetworkInspect(name))
            }
            else -> ParseResult.Failure("'docker network $sub' is not supported")
        }
    }

    private fun parseContainerCmd(args: List<String>): ParseResult {
        val sub = args.firstOrNull()?.lowercase() ?: return ParseResult.Failure("Usage: docker container COMMAND")
        return when (sub) {
            "prune" -> ParseResult.Success(DockerCommand.ContainerPrune)
            "ls", "list" -> ParseResult.Success(DockerCommand.Ps(all = "-a" in args))
            "run" -> ParseResult.Success(parseRun(args.drop(1)))
            "stop" -> {
                val targets = args.drop(1).filter { !it.startsWith("-") }
                ParseResult.Success(DockerCommand.Stop(targets))
            }
            "rm", "remove" -> {
                val force = "-f" in args
                val targets = args.drop(1).filter { !it.startsWith("-") }
                ParseResult.Success(DockerCommand.Rm(targets, force))
            }
            else -> ParseResult.Failure("'docker container $sub' is not supported")
        }
    }

    private fun parseImageCmd(args: List<String>): ParseResult {
        val sub = args.firstOrNull()?.lowercase() ?: return ParseResult.Failure("Usage: docker image COMMAND")
        return when (sub) {
            "prune" -> ParseResult.Success(DockerCommand.ImagePrune(all = "-a" in args))
            "ls", "list" -> ParseResult.Success(DockerCommand.Images())
            "rm", "remove" -> {
                val targets = args.drop(1).filter { !it.startsWith("-") }
                ParseResult.Success(DockerCommand.Rmi(targets))
            }
            "pull" -> {
                val target = args.drop(1).firstOrNull { !it.startsWith("-") }
                    ?: return ParseResult.Failure("Usage: docker image pull NAME")
                val (repo, tag) = splitImageTag(target)
                ParseResult.Success(DockerCommand.Pull(repo, tag))
            }
            else -> ParseResult.Failure("'docker image $sub' is not supported")
        }
    }

    private fun parseSystemCmd(args: List<String>): ParseResult {
        val sub = args.firstOrNull()?.lowercase() ?: return ParseResult.Failure("Usage: docker system COMMAND")
        return when (sub) {
            "prune" -> ParseResult.Success(
                DockerCommand.SystemPrune(
                    all = "-a" in args || "--all" in args,
                    withVolumes = "--volumes" in args
                )
            )
            "info" -> ParseResult.Success(DockerCommand.Info)
            "df" -> ParseResult.Success(DockerCommand.Info)
            else -> ParseResult.Failure("'docker system $sub' is not supported")
        }
    }

    private fun parseComposeSub(sub: String, args: List<String>): ParseResult = when (sub) {
        "up" -> ParseResult.Success(
            DockerCommand.ComposeUp(
                detached = "-d" in args || "--detach" in args,
                build = "--build" in args
            )
        )
        "down" -> ParseResult.Success(DockerCommand.ComposeDown(withVolumes = "-v" in args || "--volumes" in args))
        "ps" -> ParseResult.Success(DockerCommand.ComposePs)
        "logs" -> ParseResult.Success(DockerCommand.ComposeLogs(args.firstOrNull { !it.startsWith("-") }))
        "start" -> ParseResult.Success(DockerCommand.ComposeUp())
        "stop" -> ParseResult.Success(DockerCommand.ComposeDown())
        "scale" -> {
            val scaleArg = args.firstOrNull { "=" in it }
            if (scaleArg != null) {
                val service = scaleArg.substringBefore('=')
                val replicas = scaleArg.substringAfter('=').toIntOrNull()
                if (replicas != null) ParseResult.Success(DockerCommand.ComposeScale(service, replicas))
                else ParseResult.Failure("Invalid scale format. Use: docker-compose scale SERVICE=N")
            } else {
                ParseResult.Failure("Usage: docker-compose scale SERVICE=NUM")
            }
        }
        "" -> ParseResult.Failure("Usage: docker-compose COMMAND")
        else -> ParseResult.Failure("'docker-compose $sub' is not supported")
    }

    private fun parsePort(s: String): PortMapping? {
        val parts = s.split(":")
        return when (parts.size) {
            1 -> parts[0].toIntOrNull()?.let { PortMapping(it, it) }
            2 -> {
                val host = parts[0].toIntOrNull()
                val (cp, proto) = parts[1].let {
                    if ("/" in it) it.substringBefore("/").toIntOrNull() to it.substringAfter("/")
                    else it.toIntOrNull() to "tcp"
                }
                if (host != null && cp != null) PortMapping(host, cp, proto) else null
            }
            3 -> {
                val host = parts[1].toIntOrNull()
                val cp = parts[2].substringBefore("/").toIntOrNull()
                if (host != null && cp != null) PortMapping(host, cp) else null
            }
            else -> null
        }
    }

    private fun parseEnv(s: String): Pair<String, String>? {
        val eq = s.indexOf('=')
        return if (eq > 0) s.take(eq) to s.drop(eq + 1) else null
    }

    private fun parseVolume(s: String): VolumeMapping? {
        val parts = s.split(":")
        return when (parts.size) {
            1 -> VolumeMapping(parts[0], parts[0])
            2 -> VolumeMapping(parts[0], parts[1])
            3 -> VolumeMapping(parts[0], parts[1], parts[2] == "ro")
            else -> null
        }
    }

    private fun splitImageTag(s: String): Pair<String, String> {
        val lastColon = s.lastIndexOf(':')
        return if (lastColon > 0 && lastColon < s.length - 1 && '/' !in s.substring(lastColon))
            s.substring(0, lastColon) to s.substring(lastColon + 1)
        else s to "latest"
    }
}
