package com.dmarts.learndocker.data.content

import com.dmarts.learndocker.domain.model.AchievementCondition
import com.dmarts.learndocker.domain.model.AchievementDef

class AchievementRegistry {
    fun all(): List<AchievementDef> = listOf(
        AchievementDef("first_boot", "First Boot", "Run your very first Docker container", "rocket", 50,
            AchievementCondition.CommandsExecuted(1)),
        AchievementDef("runner", "Container Runner", "Execute 10 Docker commands", "terminal", 100,
            AchievementCondition.CommandsExecuted(10)),
        AchievementDef("power_user", "Power User", "Execute 50 Docker commands", "bolt", 200,
            AchievementCondition.CommandsExecuted(50)),
        AchievementDef("architect", "Platform Architect", "Execute 100 Docker commands", "architecture", 500,
            AchievementCondition.CommandsExecuted(100)),
        AchievementDef("kitchen_prep", "Day One Done", "Complete Chapter 1: First Day On The Job", "school", 150,
            AchievementCondition.ChapterCompleted("ch_01")),
        AchievementDef("fleet_manager", "Incident Responder", "Complete Chapter 2: Incident Response", "fleet", 150,
            AchievementCondition.ChapterCompleted("ch_02")),
        AchievementDef("storage_master", "Config Expert", "Complete Chapter 3: Config, Data & Networking", "storage", 150,
            AchievementCondition.ChapterCompleted("ch_03")),
        AchievementDef("depot_chief", "Image Manager", "Complete Chapter 4: Image Management", "depot", 150,
            AchievementCondition.ChapterCompleted("ch_04")),
        AchievementDef("grand_feast", "Stack Deployer", "Complete Chapter 5: Full Stack Deployment", "feast", 500,
            AchievementCondition.ChapterCompleted("ch_05")),
        AchievementDef("dabba_complete", "Docker Certified!", "Complete the full training program", "dabba", 1000,
            AchievementCondition.AllChaptersCompleted(5)),
        AchievementDef("container_factory", "Container Factory", "Create 20 containers", "factory", 200,
            AchievementCondition.ContainersCreated(20)),
        AchievementDef("sandbox_explorer", "Sandbox Explorer", "Run 25 commands in sandbox mode", "sandbox", 150,
            AchievementCondition.SandboxCommands(25)),
        AchievementDef("xp_1000", "Rising Engineer", "Earn 1000 XP", "star", 100,
            AchievementCondition.XpReached(1000)),
        AchievementDef("xp_5000", "Master Engineer", "Earn 5000 XP", "master", 300,
            AchievementCondition.XpReached(5000)),
        AchievementDef("streak_3", "3-Day Streak", "Play 3 days in a row", "flame", 100,
            AchievementCondition.StreakDays(3)),
        AchievementDef("streak_7", "Week Warrior", "Play 7 days in a row", "fire", 300,
            AchievementCondition.StreakDays(7)),
    )
}
