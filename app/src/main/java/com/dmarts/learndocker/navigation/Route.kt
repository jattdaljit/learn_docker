package com.dmarts.learndocker.navigation

sealed class Route(val path: String) {
    object Splash : Route("splash")
    object Home : Route("home")
    object ChapterMap : Route("chapter_map")

    object Story : Route("story/{chapterId}/{levelIndex}") {
        fun go(chapterId: String, levelIndex: Int) = "story/$chapterId/$levelIndex"
    }

    object Level : Route("level/{chapterId}/{levelIndex}") {
        fun go(chapterId: String, levelIndex: Int) = "level/$chapterId/$levelIndex"
    }

    object LevelComplete : Route("level_complete/{chapterId}/{levelIndex}/{xpEarned}") {
        fun go(chapterId: String, levelIndex: Int, xpEarned: Int) =
            "level_complete/$chapterId/$levelIndex/$xpEarned"
    }

    object Sandbox : Route("sandbox")
    object Achievements : Route("achievements")
    object Profile : Route("profile")
    object Commands : Route("commands")
    object DockerHub : Route("docker_hub")
    object Settings : Route("settings")
}
