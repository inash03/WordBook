package com.wordbook.domain.usecase.label

import com.wordbook.domain.model.Label
import com.wordbook.domain.repository.LabelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLabelsUseCase @Inject constructor(
    private val repository: LabelRepository
) {
    operator fun invoke(): Flow<List<Label>> = repository.getAllLabels()
}
