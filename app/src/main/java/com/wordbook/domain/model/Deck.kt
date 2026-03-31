package com.wordbook.domain.model

data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val labels: List<Label> = emptyList(),
    val cardCount: Int = 0,
    val rememberedCount: Int = 0,
    val needsReviewCount: Int = 0,
    val dueCardsCount: Int = 0
)
