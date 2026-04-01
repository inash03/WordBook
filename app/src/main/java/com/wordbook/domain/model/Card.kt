package com.wordbook.domain.model

data class Card(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val studyStatus: StudyStatus = StudyStatus.NOT_STUDIED,
    val labels: List<Label> = emptyList(),
    // SRS (SM-2) fields
    val srsInterval: Int = 0,
    val srsEaseFactor: Float = 2.5f,
    val srsDueDate: Long = 0L,
    val srsRepetitions: Int = 0
)
