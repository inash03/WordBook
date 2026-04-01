package com.wordbook.data.database.daos

import androidx.room.*
import com.wordbook.data.database.entities.TestResultEntity
import com.wordbook.data.database.entities.TestSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {

    @Query("SELECT * FROM test_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<TestSessionEntity>>

    @Query("SELECT * FROM test_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): TestSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TestSessionEntity): Long

    @Query("""
        UPDATE test_sessions SET finishedAt = :finishedAt, correctCount = :correct,
        incorrectCount = :incorrect, skippedCount = :skipped WHERE id = :sessionId
    """)
    suspend fun finishSession(
        sessionId: Long,
        finishedAt: Long,
        correct: Int,
        incorrect: Int,
        skipped: Int
    )

    @Query("DELETE FROM test_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResultEntity): Long

    @Query("SELECT * FROM test_results WHERE sessionId = :sessionId ORDER BY answeredAt ASC")
    fun getResultsForSession(sessionId: Long): Flow<List<TestResultEntity>>
}
