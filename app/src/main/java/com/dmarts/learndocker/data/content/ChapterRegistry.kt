package com.dmarts.learndocker.data.content

import com.dmarts.learndocker.domain.model.*

class ChapterRegistry {

    fun all(): List<Chapter> = listOf(
        ch01(), ch02(), ch03(), ch04(), ch05()
    )

    fun byId(id: String) = all().find { it.id == id }
    fun levelById(id: String) = all().flatMap { it.levels }.find { it.id == id }

    // Speaker color constants
    // DJ     = 0xFF0EA5E9L (sky blue)  — Senior DevOps Engineer
    // PRIYA  = 0xFF16A34AL (green)     — Backend Developer
    // SYSTEM = 0xFFD97706L (amber)     — System / tip messages

    // ─── CHAPTER 1: First Day On The Job ─────────────────────────────────────

    private fun ch01() = Chapter(
        id = "ch_01", number = 1,
        title = "First Day On The Job",
        subtitle = "Docker Basics",
        districtName = "StackForge Inc. — Engineering Onboarding",
        storyIntro = "You've just joined StackForge Inc. as a backend engineer. DJ, the senior DevOps lead, will walk you through Docker on day one. Every service at StackForge runs in a container — time to learn the ropes.",
        levels = listOf(
            Level(
                id = "ch_01_lv_01", chapterId = "ch_01", number = 1,
                title = "Verify Your Setup",
                preStoryLines = listOf(
                    StoryLine("DJ", "Welcome to the team! Before we do anything — let's confirm Docker is working on your machine.", 0xFF0EA5E9L),
                    StoryLine("DJ", "Run the hello-world container. If Docker is set up correctly, you'll see a confirmation message.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker run <image> creates a container from an image and starts it.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Docker is working. That hello-world output means the engine pulled the image and ran it successfully.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Nice! You're all set. Time to do some real work.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.RunContainer("obj1", "Run the hello-world container", imageName = "hello-world")
                ),
                hints = listOf(
                    "Use docker run followed by the image name.",
                    "Try: docker run hello-world"
                ),
                xpReward = 50
            ),
            Level(
                id = "ch_01_lv_02", chapterId = "ch_01", number = 2,
                title = "Serve the Dev Site",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "I need to review the frontend locally before the PR merge. Can you spin up an nginx container on port 80?", 0xFF16A34AL),
                    StoryLine("DJ", "Run it detached so it stays up in the background. Give it a name so it's easy to reference.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: Use -d (detached mode), --name to assign a name, and -p host:container to map a port.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "It's up! I can access it on localhost:80. Thanks.", 0xFF16A34AL),
                    StoryLine("DJ", "This is the standard pattern — named, detached, with port mapping. You'll do this constantly.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.RunContainer(
                        "obj1", "Run nginx named 'web-server' on port 80 in detached mode",
                        imageName = "nginx",
                        containerName = "web-server",
                        requireDetached = true,
                        requiredPorts = listOf(80)
                    )
                ),
                hints = listOf(
                    "Use: docker run -d --name <name> -p <hostPort>:<containerPort> <image>",
                    "Try: docker run -d --name web-server -p 80:80 nginx"
                ),
                xpReward = 100
            ),
            Level(
                id = "ch_01_lv_03", chapterId = "ch_01", number = 3,
                title = "What's Running?",
                preStoryLines = listOf(
                    StoryLine("DJ", "Before touching anything on a shared machine — always check what's already running.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Also check what images are downloaded locally. You don't want to pull something that's already there.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker ps shows running containers. docker ps -a shows ALL including stopped. docker images lists local images.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Good habit. Always inventory the environment first — saves you from stepping on someone else's work.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Exactly. Especially on a CI server or shared dev box.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.ListContainers("obj1", "List all containers including stopped ones", includeAll = true),
                    Objective.ListImages("obj2", "List all locally available Docker images")
                ),
                hints = listOf(
                    "List all containers (running + stopped): docker ps -a",
                    "List local images: docker images"
                ),
                xpReward = 75,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "web-server", "nginx:latest",
                            ContainerStatus.RUNNING, listOf(PortBinding(80, 80)))
                    ),
                    images = listOf(
                        DockerImage("sha256:abc1", "nginx", "latest", 55_000_000L),
                        DockerImage("sha256:abc2", "hello-world", "latest", 1_000_000L)
                    )
                )
            ),
            Level(
                id = "ch_01_lv_04", chapterId = "ch_01", number = 4,
                title = "Stop and Resume",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "We stop dev containers at EOD to free up resources, then restart them the next morning.", 0xFF16A34AL),
                    StoryLine("DJ", "Stop web-server now, then start it back up. You'll see the container keeps its configuration.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker stop <name> gracefully shuts down a container. docker start <name> brings it back without re-creating it.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Stopped and restarted — same container ID, same config. Nothing lost.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Stopping is not the same as removing. Always clarify which one you need.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.StopContainer("obj1", "Stop the web-server container", "web-server"),
                    Objective.StartContainer("obj2", "Start web-server again", "web-server")
                ),
                hints = listOf(
                    "Stop a container: docker stop <name>",
                    "Start it again: docker start <name>",
                    "Try: docker stop web-server — then: docker start web-server"
                ),
                xpReward = 100,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "web-server", "nginx:latest",
                            ContainerStatus.RUNNING, listOf(PortBinding(80, 80)))
                    ),
                    images = listOf(DockerImage("sha256:abc1", "nginx", "latest", 55_000_000L))
                )
            )
        )
    )

    // ─── CHAPTER 2: Incident Response ─────────────────────────────────────────

    private fun ch02() = Chapter(
        id = "ch_02", number = 2,
        title = "Incident Response",
        subtitle = "Managing Containers",
        districtName = "StackForge Inc. — On-Call Shift",
        storyIntro = "You're on-call for the first time. Services are misbehaving in staging. DJ and Priya need you to debug running containers, check logs, exec inside them, and clean up after deployments.",
        requiredChapterId = "ch_01",
        levels = listOf(
            Level(
                id = "ch_02_lv_01", chapterId = "ch_02", number = 1,
                title = "API is Throwing Errors",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "Staging API is returning 500s. Can you pull the logs from api-server? I need to see what's happening.", 0xFF16A34AL),
                    StoryLine("DJ", "Logs are always your first step in any incident. Check them before assuming anything.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker logs <name> shows the stdout/stderr output from a container.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "Found it — a missing env variable on startup. That explains the 500s.", 0xFF16A34AL),
                    StoryLine("DJ", "Good. Logs first, always. Don't restart blindly.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.ViewLogs("obj1", "View logs of the api-server container", "api-server")
                ),
                hints = listOf(
                    "View container logs: docker logs <container-name>",
                    "Try: docker logs api-server",
                    "Add -f to follow live logs: docker logs -f api-server"
                ),
                xpReward = 100,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "api-server", "node:18-alpine",
                            ContainerStatus.RUNNING, listOf(PortBinding(3000, 3000)))
                    ),
                    images = listOf(DockerImage("sha256:abc1", "node", "18-alpine", 180_000_000L))
                )
            ),
            Level(
                id = "ch_02_lv_02", chapterId = "ch_02", number = 2,
                title = "Debug Inside the Container",
                preStoryLines = listOf(
                    StoryLine("DJ", "The nginx config inside the container looks wrong. We need to exec in and check the file directly.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Can you open a shell inside web-server? That way we can inspect without redeploying.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker exec -it <name> sh opens an interactive shell inside a running container.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Found a typo in the server block. We can fix the config in the next deploy.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "exec is really useful for quick debugging. Just don't make permanent changes this way.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.ExecIntoContainer("obj1", "Execute a command inside web-server", "web-server")
                ),
                hints = listOf(
                    "Run an interactive shell inside a container: docker exec -it <name> sh",
                    "Try: docker exec -it web-server sh",
                    "Or run a one-off command: docker exec web-server ls /etc/nginx"
                ),
                xpReward = 125,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "web-server", "nginx:latest",
                            ContainerStatus.RUNNING, listOf(PortBinding(80, 80)))
                    ),
                    images = listOf(DockerImage("sha256:abc1", "nginx", "latest", 55_000_000L))
                )
            ),
            Level(
                id = "ch_02_lv_03", chapterId = "ch_02", number = 3,
                title = "Container Details for the Load Balancer",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "I'm configuring the load balancer and need the exact IP and port mapping for api-server.", 0xFF16A34AL),
                    StoryLine("DJ", "docker inspect gives you the full metadata as JSON — IP, ports, mounts, env vars, everything.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker inspect <name> returns detailed config and runtime metadata for a container.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "Got the IP — 172.17.0.2. Load balancer config updated.", 0xFF16A34AL),
                    StoryLine("DJ", "Inspect before you assume. The IP can change between restarts on the default bridge network.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.InspectResource("obj1", "Inspect the api-server container", "api-server")
                ),
                hints = listOf(
                    "Inspect a container: docker inspect <name>",
                    "Try: docker inspect api-server"
                ),
                xpReward = 100,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "api-server", "node:18-alpine",
                            ContainerStatus.RUNNING, listOf(PortBinding(3000, 3000)), ipAddress = "172.17.0.2")
                    ),
                    images = listOf(DockerImage("sha256:abc1", "node", "18-alpine", 180_000_000L))
                )
            ),
            Level(
                id = "ch_02_lv_04", chapterId = "ch_02", number = 4,
                title = "Spin Up the Dev Database",
                preStoryLines = listOf(
                    StoryLine("DJ", "Integration tests need a real Postgres instance. Spin one up locally — name it 'app-db', pass the password as an env var.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Never hardcode credentials in the command history. Use -e for environment variables.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: Use -e KEY=VALUE to inject environment variables. Postgres needs POSTGRES_PASSWORD to start.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Postgres is running. Integration tests can now connect to localhost:5432.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "And the password stayed out of the Dockerfile and the codebase. Good practice.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.RunContainer(
                        "obj1", "Run Postgres as 'app-db' with POSTGRES_PASSWORD env var",
                        imageName = "postgres",
                        containerName = "app-db",
                        requireDetached = true,
                        requiredEnvKeys = listOf("POSTGRES_PASSWORD")
                    )
                ),
                hints = listOf(
                    "Run Postgres with env variable: docker run -d --name app-db -e POSTGRES_PASSWORD=<pass> postgres:15",
                    "Try: docker run -d --name app-db -e POSTGRES_PASSWORD=secret postgres:15",
                    "Any password value works — just make sure the key is POSTGRES_PASSWORD"
                ),
                xpReward = 150
            ),
            Level(
                id = "ch_02_lv_05", chapterId = "ch_02", number = 5,
                title = "Redeploy the Service",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "The old api-server has a critical bug. We need to remove it and redeploy from the fixed image.", 0xFF16A34AL),
                    StoryLine("DJ", "Stop it first, then remove. You can't remove a running container without the force flag.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker stop <name> then docker rm <name>. Or use docker rm -f to force-remove a running container.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "Container removed. Ready to deploy the patched version.", 0xFF16A34AL),
                    StoryLine("DJ", "This is the standard redeploy pattern — stop, remove, then run the new image.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.StopContainer("obj1", "Stop the api-server container", "api-server"),
                    Objective.RemoveContainer("obj2", "Remove the api-server container", "api-server")
                ),
                hints = listOf(
                    "Stop: docker stop api-server",
                    "Remove: docker rm api-server",
                    "Or force-remove in one step: docker rm -f api-server"
                ),
                xpReward = 125,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "api-server", "node:18-alpine",
                            ContainerStatus.RUNNING, listOf(PortBinding(3000, 3000)))
                    ),
                    images = listOf(DockerImage("sha256:abc1", "node", "18-alpine", 180_000_000L))
                )
            ),
            Level(
                id = "ch_02_lv_06", chapterId = "ch_02", number = 6,
                title = "CI Runner Disk Full",
                preStoryLines = listOf(
                    StoryLine("DJ", "Our CI runner is at 95% disk usage. Stopped containers and dangling images are piling up.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Run a prune to clear them out. We do this every sprint end on all CI nodes.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker container prune removes stopped containers. docker image prune removes dangling images. Reclaims disk space fast.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Down to 60% disk usage. CI will stop failing on out-of-space errors now.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Add this to the sprint runbook. It should run automatically, but good to know manually too.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.PruneContainers("obj1", "Prune all stopped containers"),
                    Objective.PruneImages("obj2", "Prune dangling unused images")
                ),
                hints = listOf(
                    "Remove all stopped containers: docker container prune",
                    "Remove dangling images: docker image prune",
                    "Or clean everything: docker system prune"
                ),
                xpReward = 150
            )
        )
    )

    // ─── CHAPTER 3: Config, Data & Networking ─────────────────────────────────

    private fun ch03() = Chapter(
        id = "ch_03", number = 3,
        title = "Config, Data & Networking",
        subtitle = "Volumes, Env & Networks",
        districtName = "StackForge Inc. — Platform Engineering",
        storyIntro = "The platform team needs robust, production-grade container setups. That means secrets out of the codebase, persistent database storage, and isolated networks for each service tier.",
        requiredChapterId = "ch_02",
        levels = listOf(
            Level(
                id = "ch_03_lv_01", chapterId = "ch_03", number = 1,
                title = "Secrets Out of the Code",
                preStoryLines = listOf(
                    StoryLine("DJ", "We had an incident where DB credentials got committed to Git. Environment variables fix this — the image has no secrets.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Run a Redis cache container in detached mode and pass APP_ENV as an env variable.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: -e KEY=VALUE injects an env variable. You can pass multiple -e flags for multiple vars.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Config injected at runtime, not baked into the image. Any environment — dev, staging, prod — just changes the values.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "This is the 12-factor app approach. Config in env, not in code.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.RunContainer(
                        "obj1", "Run Redis named 'app-cache' with APP_ENV=production",
                        imageName = "redis",
                        containerName = "app-cache",
                        requireDetached = true,
                        requiredEnvKeys = listOf("APP_ENV")
                    )
                ),
                hints = listOf(
                    "Use -e to pass env variables: docker run -d -e APP_ENV=production redis",
                    "Full command: docker run -d --name app-cache -e APP_ENV=production redis"
                ),
                xpReward = 125
            ),
            Level(
                id = "ch_03_lv_02", chapterId = "ch_03", number = 2,
                title = "Persist Database Writes",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "Every time we restart the Postgres container in dev, all our test data is gone. We need a volume.", 0xFF16A34AL),
                    StoryLine("DJ", "Create a named volume and mount it into the container. Data will survive container removal.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker volume create <name> creates a named volume. Mount it with -v <volume>:<container-path>.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "Test data survives restarts now. My setup scripts only need to run once.", 0xFF16A34AL),
                    StoryLine("DJ", "This is the same pattern used in production. The volume lifecycle is independent of the container.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.CreateVolume("obj1", "Create a volume named 'pg-data'", "pg-data"),
                    Objective.RunContainer(
                        "obj2", "Run Postgres with the pg-data volume mounted",
                        imageName = "postgres",
                        containerName = "app-db",
                        requireDetached = true,
                        requiredEnvKeys = listOf("POSTGRES_PASSWORD"),
                        requiredVolumes = listOf("pg-data")
                    )
                ),
                hints = listOf(
                    "Create volume: docker volume create pg-data",
                    "Mount it: docker run -d --name app-db -e POSTGRES_PASSWORD=secret -v pg-data:/var/lib/postgresql/data postgres:15"
                ),
                xpReward = 175
            ),
            Level(
                id = "ch_03_lv_03", chapterId = "ch_03", number = 3,
                title = "Isolate the Test Environment",
                preStoryLines = listOf(
                    StoryLine("DJ", "Integration tests should never talk to the dev database. Create an isolated network for the test tier.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Containers on different networks can't reach each other by default — that's exactly the isolation we need.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker network create <name> creates a custom bridge network. Attach a container with --network.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "Test containers are isolated. No accidental cross-environment writes.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Network isolation is a core part of our security model. Each tier gets its own network.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.CreateNetwork("obj1", "Create a network named 'test-net'", "test-net"),
                    Objective.RunContainer(
                        "obj2", "Run nginx connected to the test-net network",
                        imageName = "nginx",
                        containerName = "test-server",
                        requireDetached = true,
                        requiredNetwork = "test-net"
                    )
                ),
                hints = listOf(
                    "Create network: docker network create test-net",
                    "Run container on that network: docker run -d --name test-server --network test-net nginx"
                ),
                xpReward = 175
            ),
            Level(
                id = "ch_03_lv_04", chapterId = "ch_03", number = 4,
                title = "Bridge the Services",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "The API server needs to reach the test database, but they're on different networks. Connect api-server to app-net.", 0xFF16A34AL),
                    StoryLine("DJ", "You can connect a running container to an additional network without restarting it.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker network connect <network> <container> — adds the container to an existing network.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "api-server can now resolve app-db by container name. DNS works within networks.", 0xFF16A34AL),
                    StoryLine("DJ", "Containers on the same network can address each other by name. No IPs needed.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.ConnectToNetwork("obj1", "Connect api-server to app-net", "api-server", "app-net")
                ),
                hints = listOf(
                    "Connect a running container to a network: docker network connect <network> <container>",
                    "Try: docker network connect app-net api-server"
                ),
                xpReward = 150,
                initialState = SimulatorState(
                    containers = listOf(
                        DockerContainer("abc123", "api-server", "node:18-alpine",
                            ContainerStatus.RUNNING, listOf(PortBinding(3000, 3000)))
                    ),
                    networks = listOf(
                        DockerNetwork(name = "app-net", driver = "bridge", subnet = "172.20.0.0/16")
                    ),
                    images = listOf(DockerImage("sha256:abc1", "node", "18-alpine", 180_000_000L))
                )
            )
        )
    )

    // ─── CHAPTER 4: Image Management ─────────────────────────────────────────

    private fun ch04() = Chapter(
        id = "ch_04", number = 4,
        title = "Image Management",
        subtitle = "Docker Hub & Images",
        districtName = "StackForge Inc. — Image Registry",
        storyIntro = "Images are the building blocks of every container. Learn to pull images from Docker Hub, choose the right tags, manage local image storage, and keep the registry clean.",
        requiredChapterId = "ch_03",
        levels = listOf(
            Level(
                id = "ch_04_lv_01", chapterId = "ch_04", number = 1,
                title = "Pull the Right Image",
                preStoryLines = listOf(
                    StoryLine("DJ", "Always use specific tags — never 'latest' in CI. Pull the redis alpine variant — it's 30MB vs 100MB.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Smaller images mean faster CI builds and less attack surface. Alpine is the default choice.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker pull <image>:<tag> downloads an image without running it. Then docker images confirms it's stored locally.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "redis:alpine downloaded. 30MB. Compare that to the full Debian-based image at 100MB+.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "Good habit — check the image size before choosing a tag.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.PullImage("obj1", "Pull the redis:alpine image", "redis"),
                    Objective.ListImages("obj2", "Verify the image is in local storage")
                ),
                hints = listOf(
                    "Pull an image: docker pull <image>:<tag>",
                    "Try: docker pull redis:alpine",
                    "Then verify: docker images"
                ),
                xpReward = 125
            ),
            Level(
                id = "ch_04_lv_02", chapterId = "ch_04", number = 2,
                title = "Free Up Disk on the Dev Machine",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "My laptop is nearly full — the hello-world image and other old images are sitting there unused. Remove them.", 0xFF16A34AL),
                    StoryLine("DJ", "You can't remove an image while a container is using it. List first, then remove.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker rmi <image> removes a local image. Check docker images first to confirm the name.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "Image removed. A few hundred MB freed up just from removing old images.", 0xFF16A34AL),
                    StoryLine("DJ", "Good habit. Run docker image prune periodically for batch cleanup.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.ListImages("obj1", "List all local images"),
                    Objective.RemoveImage("obj2", "Remove the hello-world image", "hello-world")
                ),
                hints = listOf(
                    "First list: docker images",
                    "Remove an image: docker rmi <image-name>",
                    "Try: docker rmi hello-world"
                ),
                xpReward = 125,
                initialState = SimulatorState(
                    images = listOf(
                        DockerImage("sha256:abc1", "nginx", "latest", 55_000_000L),
                        DockerImage("sha256:abc2", "hello-world", "latest", 1_000_000L),
                        DockerImage("sha256:abc3", "redis", "alpine", 30_000_000L)
                    )
                )
            )
        )
    )

    // ─── CHAPTER 5: Full Stack Deployment ────────────────────────────────────

    private fun ch05() = Chapter(
        id = "ch_05", number = 5,
        title = "Full Stack Deployment",
        subtitle = "Docker Compose",
        districtName = "StackForge Inc. — Release Engineering",
        storyIntro = "Sprint review day. The team needs the entire application stack running — API, frontend, database, and cache. Docker Compose lets you define and launch everything in one command.",
        requiredChapterId = "ch_04",
        levels = listOf(
            Level(
                id = "ch_05_lv_01", chapterId = "ch_05", number = 1,
                title = "Launch the Full Stack",
                preStoryLines = listOf(
                    StoryLine("DJ", "The docker-compose.yml defines the full stack — web, api, postgres, redis. One command starts all of them.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "This is how we run the app locally for integration testing and demos. Everyone uses compose.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker-compose up -d starts all services in detached mode as defined in docker-compose.yml.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "All four services are up. That would have taken ten separate docker run commands without Compose.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "And they're all on the same network by default — they can find each other by service name.", 0xFF16A34AL),
                ),
                objectives = listOf(
                    Objective.ComposeUp("obj1", "Start all services with docker-compose up")
                ),
                hints = listOf(
                    "Start all compose services: docker-compose up",
                    "Run detached in background: docker-compose up -d",
                    "This reads docker-compose.yml from the current directory"
                ),
                xpReward = 200
            ),
            Level(
                id = "ch_05_lv_02", chapterId = "ch_05", number = 2,
                title = "Verify All Services",
                preStoryLines = listOf(
                    StoryLine("PRIYA", "Before we present the demo, I want to confirm all services are actually running — no silent failures.", 0xFF16A34AL),
                    StoryLine("DJ", "Use both docker ps and docker-compose ps. The compose command shows you service health by name.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Tip: docker-compose ps shows service status by name. docker ps shows the actual container details.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "All services showing as Up. Good to go for the demo.", 0xFF16A34AL),
                    StoryLine("DJ", "This two-step check — docker ps plus compose ps — is standard before any demo or release.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.ListContainers("obj1", "List running containers", includeAll = false),
                    Objective.ComposePs("obj2", "Check compose service status")
                ),
                hints = listOf(
                    "List running containers: docker ps",
                    "Check all including stopped: docker ps -a",
                    "Check compose services specifically: docker-compose ps"
                ),
                xpReward = 150
            ),
            Level(
                id = "ch_05_lv_03", chapterId = "ch_05", number = 3,
                title = "Tear Down the Stack",
                preStoryLines = listOf(
                    StoryLine("DJ", "Sprint review is done. Take down the staging stack to free up resources.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "docker-compose down stops everything and removes the containers and networks Compose created.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker-compose down removes containers AND networks. Add -v to also remove the volumes.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("PRIYA", "Entire stack gone with one command. No orphaned containers or networks left behind.", 0xFF16A34AL),
                    StoryLine("DJ", "Up in one command, down in one command. That's the Compose contract.", 0xFF0EA5E9L),
                ),
                objectives = listOf(
                    Objective.ComposeDown("obj1", "Stop and remove all compose services")
                ),
                hints = listOf(
                    "Stop and remove compose stack: docker-compose down",
                    "Also remove volumes: docker-compose down -v"
                ),
                xpReward = 200
            ),
            Level(
                id = "ch_05_lv_04", chapterId = "ch_05", number = 4,
                title = "Sprint End Cleanup",
                preStoryLines = listOf(
                    StoryLine("DJ", "End of sprint. Run a full system prune — stopped containers, unused networks, dangling images, and build cache.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "We do this on every dev machine and CI runner at sprint end. Keeps things clean and fast.", 0xFF16A34AL),
                    StoryLine("SYSTEM", "Tip: docker system prune removes stopped containers, unused networks, and dangling images in one go. Add -a for unused images too.", 0xFFD97706L),
                ),
                postStoryLines = listOf(
                    StoryLine("DJ", "System cleaned. You've now covered the full Docker workflow used in production engineering.", 0xFF0EA5E9L),
                    StoryLine("PRIYA", "From running your first container to managing a full Compose stack — that's the real DevOps toolkit.", 0xFF16A34AL),
                    StoryLine("DJ", "Welcome to the team. You're ready for production.", 0xFF0EA5E9L),
                    StoryLine("SYSTEM", "Congratulations! You've completed all 20 levels. Docker fundamentals mastered.", 0xFFD97706L),
                ),
                objectives = listOf(
                    Objective.PruneSystem("obj1", "Run docker system prune to clean everything")
                ),
                hints = listOf(
                    "Clean everything: docker system prune",
                    "Also remove unused images: docker system prune -a",
                    "Confirm the prompt when asked"
                ),
                xpReward = 250
            )
        )
    )
}
