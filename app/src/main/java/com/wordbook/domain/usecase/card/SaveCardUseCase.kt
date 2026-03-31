package com.wordbook.domain.usecase.card

import com.wordbook.domain.model.Card
import com.wordbook.domain.repository.CardRepository
import javax.inject.Inject

class SaveCardUseCase @Inject constructor(
    private val repository: CardRepository
) {
    suspend operator fun invoke(card: Card, labelIds: List<Long>): Long {
        return if (card.id == 0L) {
            val id = repository.insertCard(card)
            if (labelIds.isNotEmpty()) repository.updateCardLabels(id, labelIds)
            id
        } else {
            repository.updateCard(card.copy(updatedAt = System.currentTimeMillis()))
            repository.updateCardLabels(card.id, labelIds)
            card.id
        }
    }
}
