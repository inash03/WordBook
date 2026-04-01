package com.wordbook.domain.usecase.deck

import com.wordbook.domain.repository.DeckRepository
import javax.inject.Inject

class DuplicateDeckUseCase @Inject constructor(
    private val repository: DeckRepository
) {
    suspend operator fun invoke(id: Long): Long = repository.duplicateDeck(id)
}
