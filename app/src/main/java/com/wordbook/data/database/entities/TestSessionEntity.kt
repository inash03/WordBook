package com.wordbook.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_sessions")
data class TestSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val deckName: String,
    val startedAt: Long,
    val finishedAt: Long? = null,
    val mode: String,
    val totalCards: Int,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val skippedCount: Int = 0
)
