package com.wordbook.domain.usecase.deck

import com.wordbook.domain.repository.DeckRepository
import javax.inject.Inject

class DeleteDeckUseCase @Inject constructor(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteDeck(id)
}
