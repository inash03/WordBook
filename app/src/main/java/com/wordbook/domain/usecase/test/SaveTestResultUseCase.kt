package com.wordbook.domain.usecase.test

import com.wordbook.domain.model.TestResult
import com.wordbook.domain.repository.TestRepository
import javax.inject.Inject

class SaveTestResultUseCase @Inject constructor(
    private val repository: TestRepository
) {
    suspend operator fun invoke(result: TestResult): Long = repository.insertResult(result)
}
