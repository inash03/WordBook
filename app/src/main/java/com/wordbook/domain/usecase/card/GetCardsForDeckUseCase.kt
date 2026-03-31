package com.wordbook.domain.usecase.card

import com.wordbook.domain.model.Card
import com.wordbook.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardsForDeckUseCase @Inject constructor(
    private val repository: CardRepository
) {
    operator fun invoke(deckId: Long): Flow<List<Card>> = repository.getCardsForDeck(deckId)
}
