package com.wordbook.domain.usecase.deck

import com.wordbook.domain.model.Deck
import com.wordbook.domain.repository.DeckRepository
import javax.inject.Inject

class GetDeckByIdUseCase @Inject constructor(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(id: Long): Deck? = repository.getDeckById(id)
}
