package com.dmarts.learndocker.data.repository

import com.dmarts.learndocker.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    val progressFlow: Flow<UserProgress>
    suspend fun save(progress: UserProgress)
    suspend fun update(transform: (UserProgress) -> UserProgress)
}
