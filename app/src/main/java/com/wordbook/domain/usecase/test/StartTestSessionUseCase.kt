package com.wordbook.domain.usecase.test

import com.wordbook.domain.model.TestMode
import com.wordbook.domain.model.TestSession
import com.wordbook.domain.repository.TestRepository
import javax.inject.Inject

class StartTestSessionUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(
        deckId: Long,
        deckName: String,
        mode: TestMode,
        totalCards: Int
    ): Long {
        val session = TestSession(
            deckId = deckId,
            deckName = deckName,
            startedAt = System.currentTimeMillis(),
            mode = mode,
            totalCards = totalCards
        )
        return repository.insertSession(session)
    }
}
