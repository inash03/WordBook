package com.wordbook.domain.model

data class TestSession(
    val id: Long = 0,
    val deckId: Long,
    val deckName: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val mode: TestMode,
    val totalCards: Int,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skippedCount: Int = 0
) {
    val answeredCount: Int get() = correctCount + incorrectCount + skippedCount
    val accuracyPercent: Int
        get() = if (answeredCount == 0) 0 else (correctCount * 100 / answeredCount)
}
