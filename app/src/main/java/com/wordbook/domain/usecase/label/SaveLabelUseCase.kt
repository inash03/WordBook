package com.wordbook.domain.usecase.label

import com.wordbook.domain.model.Label
import com.wordbook.domain.repository.LabelRepository
import javax.inject.Inject

class SaveLabelUseCase @Inject constructor(
    private val repository: LabelRepository
) {
    suspend operator fun invoke(label: Label): Long =
        if (label.id == 0L) repository.insertLabel(label)
        else { repository.updateLabel(label); label.id }
}
