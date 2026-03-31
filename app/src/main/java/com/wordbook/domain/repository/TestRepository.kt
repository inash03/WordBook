package com.wordbook.domain.repository

import com.wordbook.domain.model.TestResult
import com.wordbook.domain.model.TestSession
import kotlinx.coroutines.flow.Flow

interface TestRepository {
    fun getTestHistory(): Flow<List<TestSession>>
    fun getResultsForSession(sessionId: Long): Flow<List<TestResult>>
    suspend fun getSessionById(id: Long): TestSession?
    suspend fun insertSession(session: TestSession): Long
    suspend fun finishSession(sessionId: Long, finishedAt: Long, correct: Int, incorrect: Int, skipped: Int)
    suspend fun insertResult(result: TestResult): Long
    suspend fun deleteSession(sessionId: Long)
}
