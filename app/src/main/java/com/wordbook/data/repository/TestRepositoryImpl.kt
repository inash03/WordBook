package com.wordbook.data.repository

import com.wordbook.data.database.daos.TestDao
import com.wordbook.domain.model.TestResult
import com.wordbook.domain.model.TestSession
import com.wordbook.domain.repository.TestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TestRepositoryImpl @Inject constructor(
    private val testDao: TestDao
) : TestRepository {

    override fun getTestHistory(): Flow<List<TestSession>> =
        testDao.getAllSessions().map { list -> list.map { it.toDomain() } }

    override fun getResultsForSession(sessionId: Long): Flow<List<TestResult>> =
        testDao.getResultsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override suspend fun getSessionById(id: Long): TestSession? =
        testDao.getSessionById(id)?.toDomain()

    override suspend fun insertSession(session: TestSession): Long =
        testDao.insertSession(session.toEntity())

    override suspend fun finishSession(
        sessionId: Long,
        finishedAt: Long,
        correct: Int,
        incorrect: Int,
        skipped: Int
    ) = testDao.finishSession(sessionId, finishedAt, correct, incorrect, skipped)

    override suspend fun insertResult(result: TestResult): Long =
        testDao.insertResult(result.toEntity())

    override suspend fun deleteSession(sessionId: Long) =
        testDao.deleteSession(sessionId)
}
