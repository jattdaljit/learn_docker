package com.dmarts.learndocker

import android.content.Context
import com.dmarts.learndocker.data.content.AchievementRegistry
import com.dmarts.learndocker.data.content.ChapterRegistry
import com.dmarts.learndocker.data.persistence.SimulatorStateDataStore
import com.dmarts.learndocker.data.persistence.UserProgressDataStore
import com.dmarts.learndocker.data.repository.ProgressRepository
import com.dmarts.learndocker.data.repository.ProgressRepositoryImpl
import com.dmarts.learndocker.domain.command.CommandParser
import com.dmarts.learndocker.domain.engine.AchievementEngine
import com.dmarts.learndocker.domain.engine.DockerSimulator
import com.dmarts.learndocker.domain.engine.LevelValidator
import com.dmarts.learndocker.server.DockerWebServer
import kotlinx.serialization.json.Json

class AppContainer(private val applicationContext: Context) {

    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    private val dataStore = UserProgressDataStore(applicationContext, json)
    val simulatorStateDataStore = SimulatorStateDataStore(applicationContext, json)

    val progressRepository: ProgressRepository = ProgressRepositoryImpl(dataStore)

    val chapterRegistry = ChapterRegistry()
    val achievementRegistry = AchievementRegistry()

    val commandParser = CommandParser()
    val dockerSimulator = DockerSimulator()
    val levelValidator = LevelValidator()
    val achievementEngine = AchievementEngine(achievementRegistry.all())

    // ─── Web server ───────────────────────────────────────────────────────────

    private var webServer: DockerWebServer? = null
    val isWebServerRunning: Boolean get() = webServer?.isAlive == true

    /** Starts the server on [port]. Returns true on success. */
    fun startWebServer(port: Int = 8080): Boolean {
        return try {
            webServer?.stop()
            webServer = DockerWebServer(port, commandParser, dockerSimulator, applicationContext, json, chapterRegistry, progressRepository)
            webServer!!.startServer()
            true
        } catch (e: Exception) {
            webServer = null
            false
        }
    }

    fun stopWebServer() {
        webServer?.stop()
        webServer = null
    }
}
