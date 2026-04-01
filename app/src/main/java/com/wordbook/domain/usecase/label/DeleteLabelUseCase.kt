package com.wordbook.domain.usecase.label

import com.wordbook.domain.repository.LabelRepository
import javax.inject.Inject

class DeleteLabelUseCase @Inject constructor(
    private val repository: LabelRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteLabel(id)
}
