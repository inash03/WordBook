package com.wordbook.domain.usecase.test

import com.wordbook.domain.repository.TestRepository
import javax.inject.Inject

class FinishTestSessionUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(
        sessionId: Long,
        correct: Int,
        incorrect: Int,
        skipped: Int
    ) = repository.finishSession(
        sessionId = sessionId,
        finishedAt = System.currentTimeMillis(),
        correct = correct,
        incorrect = incorrect,
        skipped = skipped
    )
}
