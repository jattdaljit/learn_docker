package com.dmarts.learndocker.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProgress(
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val lastPlayedDateEpochDay: Long = -1L,
    // chapterId -> highest completed levelIndex (0-based), -1 = chapter started but no level done
    val chapterProgress: Map<String, Int> = emptyMap(),
    // levelId -> XP earned (used to check completion)
    val completedLevelIds: Set<String> = emptySet(),
    val bestTimes: Map<String, Int> = emptyMap(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val commandsExecuted: Int = 0,
    val containersCreated: Int = 0,
    val sandboxCommandsExecuted: Int = 0,
    val totalPlayTimeSeconds: Long = 0L
)

@Serializable
data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
    val xpReward: Int,
    val condition: AchievementCondition
)

@Serializable
sealed class AchievementCondition {
    @Serializable
    @SerialName("commands_executed")
    data class CommandsExecuted(val count: Int) : AchievementCondition()

    @Serializable
    @SerialName("chapter_completed")
    data class ChapterCompleted(val chapterId: String) : AchievementCondition()

    @Serializable
    @SerialName("all_chapters_completed")
    data class AllChaptersCompleted(val total: Int = 13) : AchievementCondition()

    @Serializable
    @SerialName("streak_days")
    data class StreakDays(val days: Int) : AchievementCondition()

    @Serializable
    @SerialName("containers_created")
    data class ContainersCreated(val count: Int) : AchievementCondition()

    @Serializable
    @SerialName("xp_reached")
    data class XpReached(val xp: Int) : AchievementCondition()

    @Serializable
    @SerialName("sandbox_commands")
    data class SandboxCommands(val count: Int) : AchievementCondition()
}
