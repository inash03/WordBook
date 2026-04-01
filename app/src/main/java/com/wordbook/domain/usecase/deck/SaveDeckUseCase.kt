package com.wordbook.domain.usecase.deck

import com.wordbook.domain.model.Deck
import com.wordbook.domain.repository.DeckRepository
import javax.inject.Inject

class SaveDeckUseCase @Inject constructor(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(deck: Deck, labelIds: List<Long>): Long {
        return if (deck.id == 0L) {
            val id = repository.insertDeck(deck)
            if (labelIds.isNotEmpty()) repository.updateDeckLabels(id, labelIds)
            id
        } else {
            repository.updateDeck(deck.copy(updatedAt = System.currentTimeMillis()))
            repository.updateDeckLabels(deck.id, labelIds)
            deck.id
        }
    }
}
