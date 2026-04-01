package com.wordbook.domain.usecase.test

import com.wordbook.domain.model.TestResult
import com.wordbook.domain.model.TestSession
import com.wordbook.domain.repository.TestRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTestHistoryUseCase @Inject constructor(
    private val repository: TestRepository
) {
    operator fun invoke(): Flow<List<TestSession>> = repository.getTestHistory()
    fun resultsForSession(sessionId: Long): Flow<List<TestResult>> =
        repository.getResultsForSession(sessionId)
}
