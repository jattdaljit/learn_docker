package com.dmarts.learndocker.server

import android.content.Context
import com.dmarts.learndocker.data.content.ChapterRegistry
import com.dmarts.learndocker.data.repository.ProgressRepository
import com.dmarts.learndocker.domain.command.CommandParser
import com.dmarts.learndocker.domain.command.ParseResult
import com.dmarts.learndocker.domain.engine.DockerSimulator
import com.dmarts.learndocker.domain.model.SimulatorState
import com.dmarts.learndocker.domain.model.UserProgress
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DockerWebServer(
    port: Int,
    private val commandParser: CommandParser,
    private val dockerSimulator: DockerSimulator,
    private val context: Context,
    private val json: Json,
    private val chapterRegistry: ChapterRegistry,
    private val progressRepository: ProgressRepository
) : NanoHTTPD(port) {

    @Volatile private var simulatorState = SimulatorState()

    fun startServer() = start(SOCKET_READ_TIMEOUT, false)

    // ─── Request dispatch ────────────────────────────────────────────────────

    override fun serve(session: IHTTPSession): Response {
        val r = route(session)
        r.addHeader("Access-Control-Allow-Origin", "*")
        r.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        r.addHeader("Access-Control-Allow-Headers", "Content-Type")
        return r
    }

    private fun route(session: IHTTPSession): Response {
        if (session.method == Method.OPTIONS) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "")
        }
        return try {
            when {
                session.uri == "/" || session.uri == "/index.html" ->
                    serveAsset("web/index.html", "text/html; charset=utf-8")
                session.uri == "/api/command" && session.method == Method.POST ->
                    handleCommand(session)
                session.uri == "/api/state" && session.method == Method.GET ->
                    handleState()
                session.uri == "/api/reset" && session.method == Method.POST ->
                    handleReset()
                session.uri == "/api/data" && session.method == Method.GET ->
                    handleAppData()
                session.uri == "/api/restore" && session.method == Method.POST ->
                    handleRestore(session)
                session.uri == "/sw.js" ->
                    serveAsset("web/sw.js", "text/javascript; charset=utf-8")
                else ->
                    serveAsset("web/index.html", "text/html; charset=utf-8") // SPA fallback
            }
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT,
                e.message ?: "Internal error"
            )
        }
    }

    // ─── Handlers ────────────────────────────────────────────────────────────

    private fun serveAsset(path: String, mimeType: String): Response {
        val stream = context.assets.open(path)
        return newChunkedResponse(Response.Status.OK, mimeType, stream)
    }

    @Synchronized
    private fun handleCommand(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val body = files["postData"] ?: return jsonBadRequest("Empty body")

        val req = runCatching { json.decodeFromString<WebCommandRequest>(body) }
            .getOrNull() ?: return jsonBadRequest("Invalid JSON")

        val input = req.command.trim()
        if (input.isEmpty()) {
            return jsonOk(WebCommandResponse(emptyList(), simulatorState))
        }

        val lines: List<WebLine>
        when (val result = commandParser.parse(input)) {
            is ParseResult.Failure -> {
                lines = listOf(WebLine(result.errorMessage, "error"))
            }
            is ParseResult.Success -> {
                val exec = dockerSimulator.execute(result.command, simulatorState)
                simulatorState = exec.state
                lines = exec.lines.map { mapLine(it, exec.isError) }
            }
        }

        return jsonOk(WebCommandResponse(lines, simulatorState))
    }

    private fun handleState(): Response =
        newFixedLengthResponse(
            Response.Status.OK, "application/json",
            json.encodeToString(SimulatorState.serializer(), simulatorState)
        )

    @Synchronized
    private fun handleReset(): Response {
        simulatorState = SimulatorState()
        return newFixedLengthResponse(Response.Status.OK, "application/json", """{"ok":true}""")
    }

    @Synchronized
    private fun handleRestore(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val body = files["postData"] ?: return jsonBadRequest("Empty body")
        val state = runCatching { json.decodeFromString<SimulatorState>(body) }
            .getOrNull() ?: return jsonBadRequest("Invalid state JSON")
        simulatorState = state
        return newFixedLengthResponse(Response.Status.OK, "application/json", """{"ok":true}""")
    }

    private fun handleAppData(): Response {
        val progress = runBlocking { progressRepository.progressFlow.first() }
        val chapters = chapterRegistry.all()

        val webChapters = chapters.map { ch ->
            val completedInChapter = ch.levels.count { lvl -> lvl.id in progress.completedLevelIds }
            val highestDone = progress.chapterProgress[ch.id] ?: -2
            val locked = ch.requiredChapterId != null &&
                (progress.chapterProgress[ch.requiredChapterId] ?: -2) < 0
            WebChapter(
                id = ch.id,
                number = ch.number,
                title = ch.title,
                subtitle = ch.subtitle,
                levelCount = ch.levels.size,
                completedCount = completedInChapter,
                locked = locked,
                levels = ch.levels.mapIndexed { idx, lvl ->
                    WebLevel(
                        id = lvl.id,
                        number = lvl.number,
                        title = lvl.title,
                        completed = lvl.id in progress.completedLevelIds,
                        objectiveCount = lvl.objectives.size,
                        xpReward = lvl.xpReward
                    )
                }
            )
        }

        val playerTitle = when {
            progress.totalXp >= 5000 -> "Principal Engineer"
            progress.totalXp >= 2000 -> "Senior DevOps Engineer"
            progress.totalXp >= 1000 -> "DevOps Engineer"
            progress.totalXp >= 500  -> "Junior DevOps"
            else                     -> "Engineering Trainee"
        }

        val appData = WebAppData(
            chapters = webChapters,
            totalXp = progress.totalXp,
            currentStreak = progress.currentStreak,
            playerTitle = playerTitle,
            completedLevels = progress.completedLevelIds.size,
            totalLevels = chapters.sumOf { it.levels.size }
        )

        return newFixedLengthResponse(
            Response.Status.OK, "application/json",
            json.encodeToString(WebAppData.serializer(), appData)
        )
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun mapLine(raw: String, isError: Boolean): WebLine {
        if (isError) return WebLine(raw, "error")
        return when {
            raw.startsWith('\u0011') -> WebLine(raw.drop(1), "header")
            raw.startsWith('\u0012') -> WebLine(raw.drop(1), "ok")
            raw.startsWith('\u0013') -> WebLine(raw.drop(1), "id")
            raw.startsWith('\u0014') -> WebLine(raw.drop(1), "pull")
            else                     -> WebLine(raw, "output")
        }
    }

    private fun jsonOk(response: WebCommandResponse): Response =
        newFixedLengthResponse(
            Response.Status.OK, "application/json",
            json.encodeToString(WebCommandResponse.serializer(), response)
        )

    private fun jsonBadRequest(msg: String): Response =
        newFixedLengthResponse(
            Response.Status.BAD_REQUEST, "application/json",
            """{"error":"$msg"}"""
        )
}

// ─── Wire DTOs ───────────────────────────────────────────────────────────────

@Serializable
data class WebCommandRequest(val command: String)

@Serializable
data class WebLine(val text: String, val type: String)

@Serializable
data class WebCommandResponse(
    val lines: List<WebLine>,
    val state: SimulatorState
)

@Serializable
data class WebLevel(
    val id: String,
    val number: Int,
    val title: String,
    val completed: Boolean,
    val objectiveCount: Int,
    val xpReward: Int
)

@Serializable
data class WebChapter(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String,
    val levelCount: Int,
    val completedCount: Int,
    val locked: Boolean,
    val levels: List<WebLevel>
)

@Serializable
data class WebAppData(
    val chapters: List<WebChapter>,
    val totalXp: Int,
    val currentStreak: Int,
    val playerTitle: String,
    val completedLevels: Int,
    val totalLevels: Int
)
