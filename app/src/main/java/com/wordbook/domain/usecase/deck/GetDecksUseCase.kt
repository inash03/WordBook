package com.wordbook.domain.usecase.deck

import com.wordbook.domain.model.Deck
import com.wordbook.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDecksUseCase @Inject constructor(
    private val repository: DeckRepository
) {
    operator fun invoke(query: String = "", labelIds: List<Long> = emptyList()): Flow<List<Deck>> =
        if (query.isBlank() && labelIds.isEmpty()) repository.getDecks()
        else repository.searchDecks(query, labelIds)
}
