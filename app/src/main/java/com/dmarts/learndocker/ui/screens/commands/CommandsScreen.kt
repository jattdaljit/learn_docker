package com.dmarts.learndocker.ui.screens.commands

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmarts.learndocker.ads.BannerAdView
import com.dmarts.learndocker.ui.theme.*

// ─── Data model ──────────────────────────────────────────────────────────────

data class CmdFlag(val flag: String, val description: String)

data class DockerRef(
    val command: String,
    val syntax: String,
    val description: String,
    val flags: List<CmdFlag> = emptyList(),
    val example: String = ""
)

data class CommandSection(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val commands: List<DockerRef>
)

// ─── Reference data ──────────────────────────────────────────────────────────

private val ALL_SECTIONS = listOf(

    CommandSection(
        title = "Container Management",
        icon = Icons.Default.Inbox,
        color = Color(0xFF2563EB),
        commands = listOf(
            DockerRef(
                "docker run",
                "docker run [OPTIONS] IMAGE [COMMAND] [ARG...]",
                "Creates and starts a new container from an image. This is the most commonly used Docker command — it pulls the image if not available locally, creates the container, and starts it.",
                listOf(
                    CmdFlag("-d, --detach", "Run container in background and print container ID"),
                    CmdFlag("--name", "Assign a name to the container"),
                    CmdFlag("-p, --publish", "Publish container port to host: -p hostPort:containerPort"),
                    CmdFlag("-e, --env", "Set environment variables: -e KEY=VALUE"),
                    CmdFlag("-v, --volume", "Mount a volume: -v volumeName:/path or -v /host:/container"),
                    CmdFlag("--network", "Connect container to a network"),
                    CmdFlag("--rm", "Automatically remove the container when it exits"),
                    CmdFlag("-it", "Interactive mode with a pseudo-TTY (use for shells)"),
                    CmdFlag("--restart", "Restart policy: no | always | on-failure | unless-stopped"),
                    CmdFlag("--memory", "Memory limit: --memory 512m"),
                    CmdFlag("--cpus", "CPU limit: --cpus 1.5"),
                    CmdFlag("-u, --user", "Username or UID to run as inside the container"),
                ),
                example = "docker run -d --name my-app -p 8080:80 -e ENV=prod nginx:alpine"
            ),
            DockerRef(
                "docker ps",
                "docker ps [OPTIONS]",
                "Lists containers. By default shows only running containers. Use -a to see all containers including stopped ones.",
                listOf(
                    CmdFlag("-a, --all", "Show all containers (default shows only running)"),
                    CmdFlag("-q, --quiet", "Only display container IDs"),
                    CmdFlag("--filter", "Filter output: --filter status=exited"),
                    CmdFlag("--format", "Pretty-print using a Go template"),
                    CmdFlag("-n", "Show last N created containers"),
                    CmdFlag("-s, --size", "Display total file sizes"),
                ),
                example = "docker ps -a --filter status=exited"
            ),
            DockerRef(
                "docker stop",
                "docker stop [OPTIONS] CONTAINER [CONTAINER...]",
                "Stops one or more running containers gracefully. Sends SIGTERM to the main process, waits 10 seconds, then sends SIGKILL if still running.",
                listOf(
                    CmdFlag("-t, --time", "Seconds to wait before killing (default 10): -t 30"),
                ),
                example = "docker stop my-app"
            ),
            DockerRef(
                "docker start",
                "docker start [OPTIONS] CONTAINER [CONTAINER...]",
                "Starts one or more stopped containers. The container retains its original configuration, data, and filesystem state.",
                listOf(
                    CmdFlag("-a, --attach", "Attach STDOUT/STDERR and forward signals"),
                    CmdFlag("-i, --interactive", "Attach container's STDIN"),
                ),
                example = "docker start my-app"
            ),
            DockerRef(
                "docker restart",
                "docker restart [OPTIONS] CONTAINER [CONTAINER...]",
                "Stops and then starts one or more containers. Equivalent to running docker stop followed by docker start.",
                listOf(
                    CmdFlag("-t, --time", "Seconds to wait before killing on stop (default 10)"),
                ),
                example = "docker restart my-app"
            ),
            DockerRef(
                "docker rm",
                "docker rm [OPTIONS] CONTAINER [CONTAINER...]",
                "Removes one or more stopped containers. Cannot remove a running container unless -f is used. All data inside the container is permanently deleted.",
                listOf(
                    CmdFlag("-f, --force", "Force remove a running container (sends SIGKILL)"),
                    CmdFlag("-v, --volumes", "Remove anonymous volumes associated with the container"),
                ),
                example = "docker rm -f old-container"
            ),
            DockerRef(
                "docker create",
                "docker create [OPTIONS] IMAGE [COMMAND]",
                "Creates a new container without starting it. Accepts the same options as docker run. Useful to set up a container to be started later.",
                listOf(
                    CmdFlag("--name", "Assign a name"),
                    CmdFlag("-p", "Port mapping"),
                    CmdFlag("-e", "Environment variable"),
                    CmdFlag("-v", "Volume mount"),
                ),
                example = "docker create --name my-app -p 8080:80 nginx"
            ),
            DockerRef(
                "docker rename",
                "docker rename CONTAINER NEW_NAME",
                "Renames a container. Works on both running and stopped containers.",
                example = "docker rename old-name new-name"
            ),
            DockerRef(
                "docker pause / unpause",
                "docker pause CONTAINER | docker unpause CONTAINER",
                "Pauses all processes in a container by sending SIGSTOP to every process. docker unpause resumes them. The container remains in memory but does no work while paused.",
                example = "docker pause my-app\ndocker unpause my-app"
            ),
            DockerRef(
                "docker kill",
                "docker kill [OPTIONS] CONTAINER",
                "Sends a signal to the main process of a container. Defaults to SIGKILL (immediate termination). Unlike docker stop, does not wait for graceful shutdown.",
                listOf(
                    CmdFlag("-s, --signal", "Signal to send (default KILL): -s SIGTERM"),
                ),
                example = "docker kill my-app"
            ),
            DockerRef(
                "docker port",
                "docker port CONTAINER [PRIVATE_PORT[/PROTO]]",
                "Lists port mappings or a specific port mapping for a container. Shows which host port each container port is bound to.",
                example = "docker port my-app 80"
            ),
            DockerRef(
                "docker top",
                "docker top CONTAINER [ps OPTIONS]",
                "Displays the running processes of a container, similar to the Unix 'top' or 'ps' command.",
                example = "docker top my-app"
            ),
            DockerRef(
                "docker stats",
                "docker stats [OPTIONS] [CONTAINER...]",
                "Displays a live stream of resource usage statistics (CPU, memory, network I/O, block I/O) for running containers.",
                listOf(
                    CmdFlag("--no-stream", "Disable streaming stats and only pull the first result"),
                    CmdFlag("-a, --all", "Show all containers (default shows only running)"),
                    CmdFlag("--format", "Pretty-print using a Go template"),
                ),
                example = "docker stats --no-stream"
            ),
            DockerRef(
                "docker container prune",
                "docker container prune [OPTIONS]",
                "Removes all stopped containers at once. Frees up disk space without touching running containers.",
                listOf(
                    CmdFlag("-f, --force", "Do not prompt for confirmation"),
                    CmdFlag("--filter", "Filter containers to prune: --filter until=24h"),
                ),
                example = "docker container prune -f"
            ),
        )
    ),

    CommandSection(
        title = "Image Management",
        icon = Icons.Default.Photo,
        color = Color(0xFF7C3AED),
        commands = listOf(
            DockerRef(
                "docker images",
                "docker images [OPTIONS] [REPOSITORY[:TAG]]",
                "Lists all locally stored Docker images. Shows repository, tag, image ID, creation date, and size.",
                listOf(
                    CmdFlag("-a, --all", "Show all images including intermediate layers"),
                    CmdFlag("-q, --quiet", "Only show image IDs"),
                    CmdFlag("--filter", "Filter images: --filter dangling=true"),
                    CmdFlag("--format", "Pretty-print using a Go template"),
                ),
                example = "docker images --filter dangling=false"
            ),
            DockerRef(
                "docker pull",
                "docker pull [OPTIONS] NAME[:TAG|@DIGEST]",
                "Downloads an image from a Docker registry (Docker Hub by default). Images are cached locally so subsequent runs are faster.",
                listOf(
                    CmdFlag("--all-tags, -a", "Download all tagged images in the repository"),
                    CmdFlag("--platform", "Set platform for multi-arch images: --platform linux/amd64"),
                ),
                example = "docker pull nginx:alpine\ndocker pull ubuntu:24.04"
            ),
            DockerRef(
                "docker push",
                "docker push [OPTIONS] NAME[:TAG]",
                "Uploads a local image to a Docker registry. You must be logged in (docker login) and the image name must include your registry username/org.",
                listOf(
                    CmdFlag("--all-tags, -a", "Push all tagged images in the repository"),
                ),
                example = "docker push myusername/my-app:v1.0"
            ),
            DockerRef(
                "docker build",
                "docker build [OPTIONS] PATH | URL | -",
                "Builds a Docker image from a Dockerfile. The PATH (usually .) is the build context — all files in that directory are sent to the Docker daemon.",
                listOf(
                    CmdFlag("-t, --tag", "Name and optionally tag the image: -t name:tag"),
                    CmdFlag("-f, --file", "Specify a different Dockerfile: -f Dockerfile.prod"),
                    CmdFlag("--no-cache", "Do not use cache when building layers"),
                    CmdFlag("--build-arg", "Set build-time variables: --build-arg VERSION=1.0"),
                    CmdFlag("--target", "Build a specific stage in a multi-stage Dockerfile"),
                    CmdFlag("--platform", "Set target platform: --platform linux/amd64"),
                    CmdFlag("--progress", "Set progress output: plain | tty | auto"),
                ),
                example = "docker build -t my-app:v1.0 .\ndocker build -f Dockerfile.prod -t my-app:prod ."
            ),
            DockerRef(
                "docker tag",
                "docker tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG]",
                "Creates a tag (alias) for an existing image. Used to rename images or prepare them for pushing to a registry. Does not copy the image — both tags point to the same image ID.",
                example = "docker tag my-app:latest myusername/my-app:v1.0"
            ),
            DockerRef(
                "docker rmi",
                "docker rmi [OPTIONS] IMAGE [IMAGE...]",
                "Removes one or more images from local storage. Cannot remove an image that is currently used by any container (running or stopped) unless -f is used.",
                listOf(
                    CmdFlag("-f, --force", "Force removal of the image"),
                    CmdFlag("--no-prune", "Do not delete untagged parent images"),
                ),
                example = "docker rmi nginx:old\ndocker rmi -f unused-image"
            ),
            DockerRef(
                "docker history",
                "docker history [OPTIONS] IMAGE",
                "Shows the history (layers) of an image — what commands were run at each layer and how much space each takes. Useful for understanding how an image was built.",
                listOf(
                    CmdFlag("--no-trunc", "Don't truncate output"),
                    CmdFlag("-q, --quiet", "Only show layer IDs"),
                    CmdFlag("--format", "Pretty-print using a Go template"),
                ),
                example = "docker history --no-trunc nginx"
            ),
            DockerRef(
                "docker save",
                "docker save [OPTIONS] IMAGE [IMAGE...]",
                "Saves one or more images to a tar archive. Used to transfer images between systems that can't access a registry, or for offline backups.",
                listOf(
                    CmdFlag("-o, --output", "Write to a file instead of stdout: -o my-image.tar"),
                ),
                example = "docker save -o my-app.tar my-app:v1.0"
            ),
            DockerRef(
                "docker load",
                "docker load [OPTIONS]",
                "Loads an image from a tar archive created by docker save. Restores all layers and tags.",
                listOf(
                    CmdFlag("-i, --input", "Read from a tar archive file: -i my-image.tar"),
                    CmdFlag("-q, --quiet", "Suppress the load output"),
                ),
                example = "docker load -i my-app.tar"
            ),
            DockerRef(
                "docker image prune",
                "docker image prune [OPTIONS]",
                "Removes dangling images (untagged images not referenced by any container). Add -a to also remove all unused images (not used by any container).",
                listOf(
                    CmdFlag("-a, --all", "Remove all unused images, not just dangling ones"),
                    CmdFlag("-f, --force", "Do not prompt for confirmation"),
                    CmdFlag("--filter", "Filter images: --filter until=72h"),
                ),
                example = "docker image prune -a -f"
            ),
        )
    ),

    CommandSection(
        title = "Exec, Logs & Copy",
        icon = Icons.Default.Terminal,
        color = Color(0xFF16A34A),
        commands = listOf(
            DockerRef(
                "docker exec",
                "docker exec [OPTIONS] CONTAINER COMMAND [ARG...]",
                "Runs a command inside an already running container. The container must be in the running state. Use -it for interactive sessions like shells.",
                listOf(
                    CmdFlag("-i, --interactive", "Keep STDIN open even if not attached"),
                    CmdFlag("-t, --tty", "Allocate a pseudo-TTY (use with -i for shell)"),
                    CmdFlag("-d, --detach", "Run command in the background"),
                    CmdFlag("-e, --env", "Set environment variables"),
                    CmdFlag("-u, --user", "Run as this user: -u root"),
                    CmdFlag("-w, --workdir", "Working directory inside the container"),
                ),
                example = "docker exec -it my-app sh\ndocker exec my-db mysql -u root -p"
            ),
            DockerRef(
                "docker logs",
                "docker logs [OPTIONS] CONTAINER",
                "Fetches the logs (stdout and stderr) from a container. Works for both running and stopped containers.",
                listOf(
                    CmdFlag("-f, --follow", "Follow log output in real time (like tail -f)"),
                    CmdFlag("--tail", "Show only last N lines: --tail 50"),
                    CmdFlag("--since", "Show logs since a timestamp: --since 2h"),
                    CmdFlag("--until", "Show logs before a timestamp: --until 30m"),
                    CmdFlag("-t, --timestamps", "Show timestamps on each log line"),
                ),
                example = "docker logs -f --tail 100 my-app"
            ),
            DockerRef(
                "docker attach",
                "docker attach [OPTIONS] CONTAINER",
                "Attaches your terminal's STDIN, STDOUT, and STDERR to a running container's main process. Use Ctrl+P then Ctrl+Q to detach without stopping. Preferred: use docker exec for interactive sessions.",
                listOf(
                    CmdFlag("--no-stdin", "Do not attach STDIN"),
                    CmdFlag("--sig-proxy", "Proxy all received signals to the process (default true)"),
                ),
                example = "docker attach my-app"
            ),
            DockerRef(
                "docker cp",
                "docker cp [OPTIONS] CONTAINER:SRC_PATH DEST_PATH | SRC_PATH CONTAINER:DEST_PATH",
                "Copies files or folders between a container and the local filesystem. Works for both running and stopped containers. Direction depends on argument order.",
                listOf(
                    CmdFlag("-a, --archive", "Archive mode (copy all uid/gid info)"),
                ),
                example = "docker cp my-app:/etc/nginx/nginx.conf ./nginx.conf\ndocker cp ./config.json my-app:/app/config.json"
            ),
            DockerRef(
                "docker diff",
                "docker diff CONTAINER",
                "Shows changes made to a container's filesystem since it was created. Lists files that were Added (A), Changed (C), or Deleted (D).",
                example = "docker diff my-app"
            ),
            DockerRef(
                "docker inspect",
                "docker inspect [OPTIONS] NAME|ID [NAME|ID...]",
                "Returns detailed low-level information about Docker objects (containers, images, volumes, networks) as JSON. Useful for debugging configuration, environment variables, network settings, and mounts.",
                listOf(
                    CmdFlag("-f, --format", "Format output with Go template: -f '{{.NetworkSettings.IPAddress}}'"),
                    CmdFlag("--type", "Return JSON for specified type: container | image | volume | network"),
                    CmdFlag("-s, --size", "Display total file sizes if the type is container"),
                ),
                example = "docker inspect my-app\ndocker inspect -f '{{.NetworkSettings.IPAddress}}' my-app"
            ),
        )
    ),

    CommandSection(
        title = "Volume Management",
        icon = Icons.Default.Save,
        color = Color(0xFFD97706),
        commands = listOf(
            DockerRef(
                "docker volume create",
                "docker volume create [OPTIONS] [VOLUME]",
                "Creates a named volume managed by Docker. Volumes persist data even when containers are removed, and can be shared between multiple containers.",
                listOf(
                    CmdFlag("--driver, -d", "Volume driver to use (default: local)"),
                    CmdFlag("--label", "Set metadata labels on the volume"),
                    CmdFlag("--opt, -o", "Driver-specific options"),
                ),
                example = "docker volume create my-data"
            ),
            DockerRef(
                "docker volume ls",
                "docker volume ls [OPTIONS]",
                "Lists all Docker volumes on the host.",
                listOf(
                    CmdFlag("-q, --quiet", "Only display volume names"),
                    CmdFlag("--filter", "Filter volumes: --filter dangling=true"),
                    CmdFlag("--format", "Pretty-print using a Go template"),
                ),
                example = "docker volume ls"
            ),
            DockerRef(
                "docker volume inspect",
                "docker volume inspect [OPTIONS] VOLUME [VOLUME...]",
                "Displays detailed information about a volume including its mount point on the host filesystem, driver, and any labels.",
                listOf(
                    CmdFlag("-f, --format", "Format output with Go template"),
                ),
                example = "docker volume inspect my-data"
            ),
            DockerRef(
                "docker volume rm",
                "docker volume rm [OPTIONS] VOLUME [VOLUME...]",
                "Removes one or more volumes. Cannot remove a volume that is in use by a container.",
                listOf(
                    CmdFlag("-f, --force", "Force the removal of one or more volumes"),
                ),
                example = "docker volume rm my-data"
            ),
            DockerRef(
                "docker volume prune",
                "docker volume prune [OPTIONS]",
                "Removes all unused local volumes — those not referenced by any container. This operation is irreversible and will permanently delete the data.",
                listOf(
                    CmdFlag("-f, --force", "Do not prompt for confirmation"),
                    CmdFlag("-a, --all", "Remove all volumes not used by at least one container"),
                    CmdFlag("--filter", "Filter volumes: --filter label=env=dev"),
                ),
                example = "docker volume prune -f"
            ),
        )
    ),

    CommandSection(
        title = "Network Management",
        icon = Icons.Default.Share,
        color = Color(0xFF0891B2),
        commands = listOf(
            DockerRef(
                "docker network create",
                "docker network create [OPTIONS] NETWORK",
                "Creates a new network. Containers on the same network can communicate by container name. The default driver is 'bridge' which creates an isolated network on the host.",
                listOf(
                    CmdFlag("--driver, -d", "Network driver: bridge | overlay | host | none (default: bridge)"),
                    CmdFlag("--subnet", "Subnet in CIDR format: --subnet 172.28.0.0/16"),
                    CmdFlag("--gateway", "IPv4 gateway for the master subnet"),
                    CmdFlag("--internal", "Restrict external access to the network"),
                    CmdFlag("--label", "Set metadata on the network"),
                ),
                example = "docker network create my-net\ndocker network create --subnet 172.20.0.0/16 my-net"
            ),
            DockerRef(
                "docker network ls",
                "docker network ls [OPTIONS]",
                "Lists all networks on the host. Shows network ID, name, driver, and scope. The default networks (bridge, host, none) are always present.",
                listOf(
                    CmdFlag("-q, --quiet", "Only display network IDs"),
                    CmdFlag("--filter", "Filter networks: --filter driver=bridge"),
                    CmdFlag("--format", "Pretty-print using a Go template"),
                ),
                example = "docker network ls"
            ),
            DockerRef(
                "docker network inspect",
                "docker network inspect [OPTIONS] NETWORK [NETWORK...]",
                "Displays detailed information about a network including its subnet, gateway, connected containers and their IP addresses.",
                listOf(
                    CmdFlag("-f, --format", "Format with Go template"),
                    CmdFlag("-v, --verbose", "Verbose output for diagnostics"),
                ),
                example = "docker network inspect my-net"
            ),
            DockerRef(
                "docker network connect",
                "docker network connect [OPTIONS] NETWORK CONTAINER",
                "Attaches a running container to a network. The container gets a new network interface on that network without being restarted. A container can be on multiple networks.",
                listOf(
                    CmdFlag("--alias", "Add a network-scoped alias for the container"),
                    CmdFlag("--ip", "IPv4 address: --ip 172.20.0.10"),
                    CmdFlag("--link", "Add link to another container"),
                ),
                example = "docker network connect my-net my-container"
            ),
            DockerRef(
                "docker network disconnect",
                "docker network disconnect [OPTIONS] NETWORK CONTAINER",
                "Removes a container from a network. The container's other network connections remain unaffected.",
                listOf(
                    CmdFlag("-f, --force", "Force the container to disconnect from the network"),
                ),
                example = "docker network disconnect my-net my-container"
            ),
            DockerRef(
                "docker network rm",
                "docker network rm NETWORK [NETWORK...]",
                "Removes one or more networks. Cannot remove a network that has active endpoints (containers connected to it).",
                example = "docker network rm my-net"
            ),
            DockerRef(
                "docker network prune",
                "docker network prune [OPTIONS]",
                "Removes all unused networks — those not referenced by any container.",
                listOf(
                    CmdFlag("-f, --force", "Do not prompt for confirmation"),
                    CmdFlag("--filter", "Filter networks: --filter until=24h"),
                ),
                example = "docker network prune -f"
            ),
        )
    ),

    CommandSection(
        title = "System & Info",
        icon = Icons.Default.Info,
        color = Color(0xFF64748B),
        commands = listOf(
            DockerRef(
                "docker info",
                "docker info [OPTIONS]",
                "Displays system-wide information about Docker: number of containers and images, storage driver, kernel version, OS, memory, and more. Useful for verifying Docker setup.",
                listOf(
                    CmdFlag("-f, --format", "Format output with Go template"),
                ),
                example = "docker info"
            ),
            DockerRef(
                "docker version",
                "docker version [OPTIONS]",
                "Shows Docker version information for both the client and server (daemon). Includes version number, API version, Go version, OS/arch, and build date.",
                listOf(
                    CmdFlag("-f, --format", "Format output with Go template"),
                ),
                example = "docker version"
            ),
            DockerRef(
                "docker system df",
                "docker system df [OPTIONS]",
                "Shows disk usage by Docker — how much space is used by images, containers, and volumes, and how much is reclaimable.",
                listOf(
                    CmdFlag("-v, --verbose", "Show detailed space usage per object"),
                ),
                example = "docker system df -v"
            ),
            DockerRef(
                "docker system prune",
                "docker system prune [OPTIONS]",
                "The ultimate cleanup command. Removes all stopped containers, all unused networks, all dangling images, and the build cache. Add -a to also remove all unused images.",
                listOf(
                    CmdFlag("-a, --all", "Remove all unused images, not just dangling ones"),
                    CmdFlag("-f, --force", "Do not prompt for confirmation"),
                    CmdFlag("--volumes", "Also prune anonymous volumes"),
                    CmdFlag("--filter", "Filter objects: --filter until=24h"),
                ),
                example = "docker system prune -a -f --volumes"
            ),
            DockerRef(
                "docker events",
                "docker events [OPTIONS]",
                "Streams real-time events from the Docker daemon — container lifecycle, image events, volume and network events. Useful for monitoring and automation.",
                listOf(
                    CmdFlag("--since", "Show all events created since a timestamp"),
                    CmdFlag("--until", "Stream events until a timestamp"),
                    CmdFlag("--filter", "Filter events: --filter type=container"),
                    CmdFlag("-f, --format", "Format output with Go template"),
                ),
                example = "docker events --filter type=container --filter event=start"
            ),
            DockerRef(
                "docker login",
                "docker login [OPTIONS] [SERVER]",
                "Logs in to a Docker registry. Without a server argument, logs in to Docker Hub. Credentials are stored in ~/.docker/config.json.",
                listOf(
                    CmdFlag("-u, --username", "Username"),
                    CmdFlag("-p, --password", "Password (not recommended — use stdin instead)"),
                    CmdFlag("--password-stdin", "Take password from stdin for security"),
                ),
                example = "echo \$DOCKER_PASS | docker login -u myuser --password-stdin"
            ),
            DockerRef(
                "docker logout",
                "docker logout [SERVER]",
                "Logs out from a Docker registry by removing credentials from ~/.docker/config.json.",
                example = "docker logout"
            ),
        )
    ),

    CommandSection(
        title = "Docker Compose",
        icon = Icons.Default.LibraryBooks,
        color = Color(0xFFE11D48),
        commands = listOf(
            DockerRef(
                "docker compose up",
                "docker compose up [OPTIONS] [SERVICE...]",
                "Builds, (re)creates, starts, and attaches to containers for all services defined in docker-compose.yml. If services are already running, it updates them if the config changed.",
                listOf(
                    CmdFlag("-d, --detach", "Run containers in the background"),
                    CmdFlag("--build", "Build images before starting containers"),
                    CmdFlag("--force-recreate", "Recreate containers even if config/image hasn't changed"),
                    CmdFlag("--no-deps", "Don't start linked services"),
                    CmdFlag("--scale", "Scale a service: --scale web=3"),
                    CmdFlag("--remove-orphans", "Remove containers for services not in the compose file"),
                ),
                example = "docker compose up -d\ndocker compose up --build --force-recreate"
            ),
            DockerRef(
                "docker compose down",
                "docker compose down [OPTIONS]",
                "Stops and removes containers, networks, and by default anonymous volumes created by docker compose up. Named volumes are kept unless --volumes is used.",
                listOf(
                    CmdFlag("-v, --volumes", "Remove named volumes declared in the compose file"),
                    CmdFlag("--rmi", "Remove images used by services: all | local"),
                    CmdFlag("--remove-orphans", "Remove containers for services not defined in the file"),
                    CmdFlag("-t, --timeout", "Specify shutdown timeout in seconds (default 10)"),
                ),
                example = "docker compose down -v --rmi all"
            ),
            DockerRef(
                "docker compose ps",
                "docker compose ps [OPTIONS] [SERVICE...]",
                "Lists containers for the current compose project with their status, ports, and command.",
                listOf(
                    CmdFlag("-a, --all", "Show all stopped containers (including run-once services)"),
                    CmdFlag("-q, --quiet", "Only show container IDs"),
                    CmdFlag("--format", "Format output: table | json"),
                    CmdFlag("--status", "Filter by status: running | stopped | paused | exited"),
                ),
                example = "docker compose ps"
            ),
            DockerRef(
                "docker compose logs",
                "docker compose logs [OPTIONS] [SERVICE...]",
                "Shows log output from services. Without a service name, shows logs from all services.",
                listOf(
                    CmdFlag("-f, --follow", "Follow log output in real time"),
                    CmdFlag("--tail", "Number of lines to show from the end: --tail 50"),
                    CmdFlag("-t, --timestamps", "Show timestamps"),
                    CmdFlag("--no-color", "Produce monochrome output"),
                ),
                example = "docker compose logs -f web\ndocker compose logs --tail 100"
            ),
            DockerRef(
                "docker compose exec",
                "docker compose exec [OPTIONS] SERVICE COMMAND",
                "Executes a command in a running compose service container. Similar to docker exec but uses the service name instead of container name.",
                listOf(
                    CmdFlag("-it", "Interactive with TTY (for shell sessions)"),
                    CmdFlag("-u, --user", "Run as specified username or UID"),
                    CmdFlag("-e, --env", "Set environment variable"),
                    CmdFlag("-w, --workdir", "Working directory"),
                ),
                example = "docker compose exec web sh\ndocker compose exec db mysql -u root"
            ),
            DockerRef(
                "docker compose build",
                "docker compose build [OPTIONS] [SERVICE...]",
                "Builds or rebuilds service images defined in the compose file. Uses the Dockerfile specified in the build section.",
                listOf(
                    CmdFlag("--no-cache", "Do not use cache when building"),
                    CmdFlag("--pull", "Always attempt to pull a newer version of the base image"),
                    CmdFlag("--push", "Push service images after building"),
                    CmdFlag("--progress", "Set type of progress output: plain | tty | json | quiet"),
                ),
                example = "docker compose build --no-cache web"
            ),
            DockerRef(
                "docker compose pull",
                "docker compose pull [OPTIONS] [SERVICE...]",
                "Pulls images for services defined in the compose file without starting them.",
                listOf(
                    CmdFlag("-q, --quiet", "Pull without printing progress information"),
                    CmdFlag("--ignore-pull-failures", "Pull what it can and ignore images with pull failures"),
                ),
                example = "docker compose pull"
            ),
            DockerRef(
                "docker compose restart",
                "docker compose restart [OPTIONS] [SERVICE...]",
                "Restarts all stopped and running service containers. Does not re-apply config changes — use up to apply config changes.",
                listOf(
                    CmdFlag("-t, --timeout", "Specify shutdown timeout in seconds (default 10)"),
                ),
                example = "docker compose restart web"
            ),
            DockerRef(
                "docker compose stop",
                "docker compose stop [OPTIONS] [SERVICE...]",
                "Stops running containers without removing them. Containers can be restarted with docker compose start.",
                listOf(
                    CmdFlag("-t, --timeout", "Specify shutdown timeout in seconds"),
                ),
                example = "docker compose stop"
            ),
            DockerRef(
                "docker compose rm",
                "docker compose rm [OPTIONS] [SERVICE...]",
                "Removes stopped compose containers. Does not remove volumes by default.",
                listOf(
                    CmdFlag("-f, --force", "Don't ask to confirm removal"),
                    CmdFlag("-v", "Remove anonymous volumes attached to containers"),
                    CmdFlag("-s, --stop", "Stop the containers, if required, before removing"),
                ),
                example = "docker compose rm -fsv"
            ),
        )
    ),

    CommandSection(
        title = "Dockerfile Instructions",
        icon = Icons.Default.Description,
        color = Color(0xFF059669),
        commands = listOf(
            DockerRef(
                "FROM",
                "FROM [--platform=<platform>] <image>[:<tag>] [AS <name>]",
                "Sets the base image for the build. Every Dockerfile must start with FROM. Using 'AS name' creates a named stage for multi-stage builds.",
                listOf(
                    CmdFlag("--platform", "Specify platform for multi-arch: --platform=linux/amd64"),
                    CmdFlag("AS name", "Name this stage for use with --target or COPY --from"),
                ),
                example = "FROM ubuntu:24.04\nFROM node:20-alpine AS builder"
            ),
            DockerRef(
                "RUN",
                "RUN <command> (shell form)\nRUN [\"executable\", \"param1\"] (exec form)",
                "Executes commands in a new layer on top of the current image and commits the result. Shell form uses /bin/sh -c. Chain commands with && to reduce layers. Each RUN creates a new layer.",
                example = "RUN apt-get update && apt-get install -y curl \\\n    && rm -rf /var/lib/apt/lists/*"
            ),
            DockerRef(
                "COPY",
                "COPY [--chown=user:group] <src> <dest>",
                "Copies files or directories from the build context (or another build stage) to the image filesystem. Preferred over ADD for simple file copying.",
                listOf(
                    CmdFlag("--chown", "Set ownership: --chown=node:node"),
                    CmdFlag("--from", "Copy from another stage: --from=builder"),
                    CmdFlag("--chmod", "Set permissions: --chmod=755"),
                ),
                example = "COPY package*.json ./\nCOPY --from=builder /app/dist ./dist"
            ),
            DockerRef(
                "ADD",
                "ADD [--chown=user:group] <src> <dest>",
                "Like COPY, but also supports URL sources and automatically extracts tar archives. Prefer COPY for simple file operations — ADD adds complexity. Use ADD only when you need tar auto-extraction.",
                example = "ADD https://example.com/file.tar.gz /tmp/\nADD app.tar.gz /app/"
            ),
            DockerRef(
                "CMD",
                "CMD [\"executable\", \"param1\"] (exec form — preferred)\nCMD command param1 (shell form)",
                "Provides the default command to run when a container starts. Only the last CMD matters. Can be overridden by arguments to docker run. If you also have ENTRYPOINT, CMD provides default arguments to it.",
                example = "CMD [\"node\", \"server.js\"]\nCMD [\"--port\", \"8080\"]  # default args for ENTRYPOINT"
            ),
            DockerRef(
                "ENTRYPOINT",
                "ENTRYPOINT [\"executable\", \"param1\"] (exec form — preferred)\nENTRYPOINT command param1 (shell form)",
                "Configures the container to run as an executable. Unlike CMD, it cannot be easily overridden by docker run arguments — those are appended as arguments instead. Use with CMD for defaults.",
                example = "ENTRYPOINT [\"node\"]\nCMD [\"server.js\"]  # docker run myapp other.js → runs 'node other.js'"
            ),
            DockerRef(
                "ENV",
                "ENV <key>=<value> ...",
                "Sets environment variables that persist in the container and are available to all subsequent instructions and to the running container. Prefer --env-file or -e at runtime for secrets.",
                example = "ENV NODE_ENV=production \\\n    PORT=8080 \\\n    APP_DIR=/app"
            ),
            DockerRef(
                "EXPOSE",
                "EXPOSE <port>[/<protocol>]",
                "Documents which port the container listens on at runtime. This is informational only — it does NOT actually publish the port. Use -p when running the container to publish ports.",
                example = "EXPOSE 80\nEXPOSE 443/tcp\nEXPOSE 5353/udp"
            ),
            DockerRef(
                "VOLUME",
                "VOLUME [\"/data\"]",
                "Creates a mount point and marks it as externally mountable. Docker automatically creates an anonymous volume for this path if no volume is provided at runtime. Good for database data directories.",
                example = "VOLUME [\"/var/lib/mysql\"]\nVOLUME [\"/data\", \"/logs\"]"
            ),
            DockerRef(
                "WORKDIR",
                "WORKDIR /path/to/workdir",
                "Sets the working directory for RUN, CMD, ENTRYPOINT, COPY, and ADD instructions that follow it. Created automatically if it doesn't exist. Prefer over cd in RUN commands.",
                example = "WORKDIR /app\nCOPY . .\nRUN npm install"
            ),
            DockerRef(
                "ARG",
                "ARG <name>[=<default value>]",
                "Defines a build-time variable that can be passed with docker build --build-arg. Unlike ENV, ARG variables are not available in the running container. Useful for build customization.",
                example = "ARG NODE_VERSION=20\nFROM node:\$NODE_VERSION-alpine"
            ),
            DockerRef(
                "HEALTHCHECK",
                "HEALTHCHECK [OPTIONS] CMD command\nHEALTHCHECK NONE",
                "Tells Docker how to test whether the container is healthy. Docker runs the command periodically and marks the container as healthy or unhealthy based on the exit code (0=healthy, 1=unhealthy).",
                listOf(
                    CmdFlag("--interval", "Time between checks (default 30s)"),
                    CmdFlag("--timeout", "Time before check is killed (default 30s)"),
                    CmdFlag("--start-period", "Initialization time before checks count (default 0s)"),
                    CmdFlag("--retries", "Consecutive failures before marking unhealthy (default 3)"),
                ),
                example = "HEALTHCHECK --interval=30s --timeout=5s \\\n  CMD curl -f http://localhost/ || exit 1"
            ),
            DockerRef(
                "USER",
                "USER <user>[:<group>] or USER <UID>[:<GID>]",
                "Sets the user (and optionally group) that subsequent RUN, CMD, and ENTRYPOINT instructions run as. Security best practice: don't run containers as root.",
                example = "RUN groupadd -r app && useradd -r -g app app\nUSER app"
            ),
            DockerRef(
                "LABEL",
                "LABEL <key>=<value> ...",
                "Adds metadata to an image as key-value pairs. Useful for documenting the image maintainer, version, and other metadata. Use docker inspect to view labels.",
                example = "LABEL maintainer=\"team@stackforge.io\" \\\n      version=\"1.0\" \\\n      description=\"StackForge web service\""
            ),
            DockerRef(
                "ONBUILD",
                "ONBUILD <INSTRUCTION>",
                "Adds a trigger instruction that runs when the image is used as the base for another build. Useful for creating base images that automatically set up project files.",
                example = "ONBUILD COPY . /app\nONBUILD RUN npm install"
            ),
        )
    ),
)

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    val filteredSections = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ALL_SECTIONS
        } else {
            val q = searchQuery.trim().lowercase()
            ALL_SECTIONS.mapNotNull { section ->
                val matchedCmds = section.commands.filter { cmd ->
                    cmd.command.lowercase().contains(q) ||
                    cmd.description.lowercase().contains(q) ||
                    cmd.flags.any { it.flag.lowercase().contains(q) || it.description.lowercase().contains(q) }
                }
                if (matchedCmds.isNotEmpty()) section.copy(commands = matchedCmds) else null
            }
        }
    }

    // Auto-expand all sections when searching
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            ALL_SECTIONS.forEach { expandedSections[it.title] = true }
        }
    }

    Scaffold(
        containerColor = NeoBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = NeoTextSecondary)
                    }
                },
                title = {
                    Column {
                        Text("Docker Reference", color = NeoTextPrimary, fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold)
                        Text("${ALL_SECTIONS.sumOf { it.commands.size }} commands & instructions",
                            color = NeoTextSecondary, fontSize = 11.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NeoSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ─── Search bar ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeoSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null,
                    tint = NeoTextSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = NeoTextPrimary, fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(NeoCyan),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (searchQuery.isEmpty()) {
                                Text("Search commands, flags, descriptions...",
                                    color = NeoTextMuted, fontSize = 14.sp)
                            }
                            inner()
                        }
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, "Clear", tint = NeoTextSecondary,
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
            HorizontalDivider(color = NeoBorder)

            // ─── Content ──────────────────────────────────────────────────
            if (filteredSections.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Block, null, tint = NeoTextMuted,
                            modifier = Modifier.size(48.dp))
                        Text("No commands found", color = NeoTextSecondary, fontSize = 15.sp)
                        Text("Try searching for: run, logs, volume, network",
                            color = NeoTextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    filteredSections.forEach { section ->
                        val isExpanded = expandedSections[section.title] ?: false
                        item(key = section.title) {
                            SectionHeader(
                                section = section,
                                isExpanded = isExpanded,
                                commandCount = section.commands.size,
                                onClick = {
                                    expandedSections[section.title] = !isExpanded
                                }
                            )
                        }
                        if (isExpanded) {
                            items(section.commands, key = { "${section.title}/${it.command}" }) { cmd ->
                                CommandCard(cmd = cmd, accentColor = section.color)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reference content without its own Scaffold — used as a tab in MainScreen.
 */
@Composable
fun CommandsScreenContent() {
    var searchQuery by remember { mutableStateOf("") }
    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }

    val filteredSections = remember(searchQuery) {
        if (searchQuery.isBlank()) ALL_SECTIONS
        else {
            val q = searchQuery.trim().lowercase()
            ALL_SECTIONS.mapNotNull { section ->
                val matched = section.commands.filter { cmd ->
                    cmd.command.lowercase().contains(q) ||
                    cmd.description.lowercase().contains(q) ||
                    cmd.flags.any { it.flag.lowercase().contains(q) || it.description.lowercase().contains(q) }
                }
                if (matched.isNotEmpty()) section.copy(commands = matched) else null
            }
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) ALL_SECTIONS.forEach { expandedSections[it.title] = true }
    }

    Column(modifier = Modifier.fillMaxSize().background(NeoBackground)) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeoSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Docker Reference", color = NeoTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("${ALL_SECTIONS.sumOf { it.commands.size }} commands & instructions",
                    color = NeoTextSecondary, fontSize = 11.sp)
            }
        }
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeoSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = NeoTextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(color = NeoTextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(NeoCyan),
                singleLine = true,
                decorationBox = { inner ->
                    Box {
                        if (searchQuery.isEmpty()) Text("Search commands, flags...", color = NeoTextMuted, fontSize = 14.sp)
                        inner()
                    }
                }
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Clear", tint = NeoTextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
        HorizontalDivider(color = NeoBorder)

        if (filteredSections.isEmpty()) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No commands found", color = NeoTextSecondary, fontSize = 15.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp)) {
                filteredSections.forEach { section ->
                    val isExpanded = expandedSections[section.title] ?: false
                    item(key = section.title) {
                        SectionHeader(section, isExpanded, section.commands.size) {
                            expandedSections[section.title] = !isExpanded
                        }
                    }
                    if (isExpanded) {
                        items(section.commands, key = { "${section.title}/${it.command}" }) { cmd ->
                            CommandCard(cmd = cmd, accentColor = section.color)
                        }
                    }
                }
            }
        }

        // ── Banner ad — always visible at bottom ──
        BannerAdView()
    }
}

@Composable
private fun SectionHeader(
    section: CommandSection,
    isExpanded: Boolean,
    commandCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(NeoSurface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(section.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(section.icon, contentDescription = null, tint = section.color,
                modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(section.title, color = NeoTextPrimary, fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold)
            Text("$commandCount ${if (commandCount == 1) "command" else "commands"}",
                color = NeoTextSecondary, fontSize = 11.sp)
        }
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = NeoTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
    HorizontalDivider(color = NeoBorder, thickness = 0.5.dp)
}

@Composable
private fun CommandCard(cmd: DockerRef, accentColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .background(NeoBackground)
            .padding(horizontal = 16.dp, vertical = 0.dp)
    ) {
        // Command row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
                    .align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Command name chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor.copy(alpha = 0.10f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        cmd.command,
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    cmd.description,
                    color = NeoTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 2
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = NeoTextMuted,
                modifier = Modifier.size(16.dp).padding(top = 2.dp)
            )
        }

        // Expanded details
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, end = 0.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Syntax block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A1D2E))
                        .padding(10.dp)
                ) {
                    Text("SYNTAX", color = Color(0xFF5A6380), fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        cmd.syntax,
                        color = Color(0xFF7DD3FC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }

                // Flags
                if (cmd.flags.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeoSurface)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("FLAGS", color = NeoTextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        cmd.flags.forEach { flag ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    flag.flag,
                                    color = Color(0xFFFBBF24),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.widthIn(min = 140.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    flag.description,
                                    color = NeoTextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Example
                if (cmd.example.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1A1D2E))
                            .padding(10.dp)
                    ) {
                        Text("EXAMPLE", color = Color(0xFF5A6380), fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        cmd.example.split("\n").forEach { line ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("$ ", color = Color(0xFF6EE7B7),
                                    fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold)
                                Text(line, color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = NeoBorder, thickness = 0.5.dp)
    }
}
