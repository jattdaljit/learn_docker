package com.dmarts.learndocker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dmarts.learndocker.ui.screens.achievements.AchievementsScreen
import com.dmarts.learndocker.ui.screens.chaptermap.ChapterMapScreen
import com.dmarts.learndocker.ui.screens.commands.CommandsScreen
import com.dmarts.learndocker.ui.screens.hub.DockerHubScreen
import com.dmarts.learndocker.ui.screens.level.LevelScreen
import com.dmarts.learndocker.ui.screens.levelcomplete.LevelCompleteScreen
import com.dmarts.learndocker.ui.screens.main.MainScreen
import com.dmarts.learndocker.ui.screens.profile.ProfileScreen
import com.dmarts.learndocker.ui.screens.sandbox.SandboxScreen
import com.dmarts.learndocker.ui.screens.settings.SettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Home.path) {

        composable(Route.Home.path) {
            MainScreen(
                onChapterClick = { chapterId ->
                    navController.navigate("chapter_map/$chapterId")
                },
                onAchievementsClick = { navController.navigate(Route.Achievements.path) },
                onProfileClick = { navController.navigate(Route.Profile.path) },
                onSettingsClick = { navController.navigate(Route.Settings.path) }
            )
        }

        composable(
            "chapter_map/{chapterId}",
            arguments = listOf(navArgument("chapterId") { type = NavType.StringType })
        ) { back ->
            val chapterId = back.arguments?.getString("chapterId") ?: return@composable
            ChapterMapScreen(
                chapterId = chapterId,
                onLevelSelect = { cId, levelIdx ->
                    navController.navigate(Route.Level.go(cId, levelIdx))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Route.Level.path,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("levelIndex") { type = NavType.IntType }
            )
        ) { back ->
            val chapterId = back.arguments?.getString("chapterId") ?: return@composable
            val levelIndex = back.arguments?.getInt("levelIndex") ?: 0
            LevelScreen(
                chapterId = chapterId,
                levelIndex = levelIndex,
                onBack = { navController.popBackStack() },
                onLevelComplete = { cId, idx, xp ->
                    navController.navigate(Route.LevelComplete.go(cId, idx, xp)) {
                        popUpTo(Route.Level.go(cId, idx)) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Route.LevelComplete.path,
            arguments = listOf(
                navArgument("chapterId") { type = NavType.StringType },
                navArgument("levelIndex") { type = NavType.IntType },
                navArgument("xpEarned") { type = NavType.IntType }
            )
        ) { back ->
            val chapterId = back.arguments?.getString("chapterId") ?: return@composable
            val levelIndex = back.arguments?.getInt("levelIndex") ?: 0
            val xpEarned = back.arguments?.getInt("xpEarned") ?: 0
            LevelCompleteScreen(
                chapterId = chapterId,
                levelIndex = levelIndex,
                xpEarned = xpEarned,
                onNextLevel = {
                    navController.navigate(Route.Level.go(chapterId, levelIndex + 1)) {
                        popUpTo("chapter_map/$chapterId")
                    }
                },
                onChapterMap = {
                    navController.navigate("chapter_map/$chapterId") {
                        popUpTo("chapter_map/$chapterId") { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Home.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Sandbox.path) {
            SandboxScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Achievements.path) {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Profile.path) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.Commands.path) {
            CommandsScreen(onBack = { navController.popBackStack() })
        }

        composable(Route.DockerHub.path) {
            DockerHubScreen(
                onBack = { navController.popBackStack() },
                onTryInSandbox = {
                    navController.navigate(Route.Sandbox.path) {
                        popUpTo(Route.DockerHub.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Settings.path) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
