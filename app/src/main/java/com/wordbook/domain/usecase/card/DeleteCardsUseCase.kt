package com.wordbook.domain.usecase.card

import com.wordbook.domain.repository.CardRepository
import javax.inject.Inject

class DeleteCardsUseCase @Inject constructor(
    private val repository: CardRepository
) {
    suspend operator fun invoke(ids: List<Long>) = repository.deleteCards(ids)
    suspend fun single(id: Long) = repository.deleteCard(id)
}
