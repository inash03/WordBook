package com.wordbook.domain.model

data class TestResult(
    val id: Long = 0,
    val sessionId: Long,
    val cardId: Long,
    val cardFront: String,
    val cardBack: String,
    val result: ResultType,
    val answeredAt: Long
)
