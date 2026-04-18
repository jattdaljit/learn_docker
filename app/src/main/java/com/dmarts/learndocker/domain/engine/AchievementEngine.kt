package com.dmarts.learndocker.domain.engine

import com.dmarts.learndocker.domain.model.AchievementCondition
import com.dmarts.learndocker.domain.model.AchievementDef
import com.dmarts.learndocker.domain.model.UserProgress

class AchievementEngine(private val registry: List<AchievementDef>) {

    fun evaluate(progress: UserProgress): List<AchievementDef> =
        registry.filter { def ->
            def.id !in progress.unlockedAchievementIds && isMet(def.condition, progress)
        }

    private fun isMet(condition: AchievementCondition, p: UserProgress): Boolean = when (condition) {
        is AchievementCondition.CommandsExecuted -> p.commandsExecuted >= condition.count
        is AchievementCondition.ChapterCompleted -> {
            val progress = p.chapterProgress[condition.chapterId] ?: -1
            progress >= 0
        }
        is AchievementCondition.AllChaptersCompleted -> p.chapterProgress.size >= condition.total
        is AchievementCondition.StreakDays -> p.currentStreak >= condition.days
        is AchievementCondition.ContainersCreated -> p.containersCreated >= condition.count
        is AchievementCondition.XpReached -> p.totalXp >= condition.xp
        is AchievementCondition.SandboxCommands -> p.sandboxCommandsExecuted >= condition.count
    }
}
