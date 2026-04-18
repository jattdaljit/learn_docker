package com.dmarts.learndocker.data.repository

import com.dmarts.learndocker.data.persistence.UserProgressDataStore
import com.dmarts.learndocker.domain.model.UserProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ProgressRepositoryImpl(
    private val dataStore: UserProgressDataStore
) : ProgressRepository {

    override val progressFlow: Flow<UserProgress> = dataStore.progressFlow

    override suspend fun save(progress: UserProgress) = dataStore.save(progress)

    override suspend fun update(transform: (UserProgress) -> UserProgress) {
        val current = dataStore.progressFlow.first()
        dataStore.save(transform(current))
    }
}
