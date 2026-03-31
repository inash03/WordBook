package com.wordbook.domain.usecase.card

import com.wordbook.domain.model.StudyStatus
import com.wordbook.domain.repository.CardRepository
import javax.inject.Inject

class UpdateCardStudyStatusUseCase @Inject constructor(
    private val repository: CardRepository
) {
    suspend operator fun invoke(cardId: Long, status: StudyStatus) =
        repository.updateCardStudyStatus(cardId, status)
}
