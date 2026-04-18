package com.dmarts.learndocker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmarts.learndocker.AppContainer
import com.dmarts.learndocker.domain.model.Chapter
import com.dmarts.learndocker.domain.model.UserProgress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val chapters: List<Chapter> = emptyList(),
    val progress: UserProgress = UserProgress(),
    val playerTitle: String = "Recruit",
    val currentLevelLabel: String = "Ch.1 - First Day On The Job",
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        val chapters = container.chapterRegistry.all()
        viewModelScope.launch {
            container.progressRepository.progressFlow.collect { progress ->
                val title = when {
                    progress.totalXp >= 5000 -> "Principal Engineer"
                    progress.totalXp >= 2000 -> "Senior DevOps Engineer"
                    progress.totalXp >= 1000 -> "DevOps Engineer"
                    progress.totalXp >= 500  -> "Junior DevOps"
                    else -> "Engineering Trainee"
                }
                val currentChapter = chapters.firstOrNull { ch ->
                    progress.chapterProgress[ch.id] == null ||
                    (progress.chapterProgress[ch.id] ?: -1) < ch.levels.size - 1
                } ?: chapters.last()
                val completedLevels = progress.chapterProgress[currentChapter.id] ?: -1
                val nextLevelNum = (completedLevels + 2).coerceAtMost(currentChapter.levels.size)
                _state.update { it.copy(
                    chapters = chapters,
                    progress = progress,
                    playerTitle = title,
                    currentLevelLabel = "Ch.${currentChapter.number} L${nextLevelNum}"
                )}
            }
        }
        viewModelScope.launch { updateStreak() }
    }

    private suspend fun updateStreak() {
        container.progressRepository.update { p ->
            val today = LocalDate.now().toEpochDay()
            val streak = when (today - p.lastPlayedDateEpochDay) {
                0L -> p.currentStreak
                1L -> p.currentStreak + 1
                else -> 1
            }
            p.copy(currentStreak = streak, lastPlayedDateEpochDay = today)
        }
    }

    fun isChapterUnlocked(chapter: Chapter, progress: UserProgress): Boolean {
        val req = chapter.requiredChapterId ?: return true
        return (progress.chapterProgress[req] ?: -1) >= 0
    }

    fun getChapterCompletionPercent(chapter: Chapter, progress: UserProgress): Float {
        val completed = progress.chapterProgress[chapter.id]?.plus(1) ?: 0
        return (completed.toFloat() / chapter.levels.size).coerceIn(0f, 1f)
    }
}
