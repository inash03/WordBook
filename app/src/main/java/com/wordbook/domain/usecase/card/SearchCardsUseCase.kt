package com.wordbook.domain.usecase.card

import com.wordbook.domain.model.Card
import com.wordbook.domain.model.StudyStatus
import com.wordbook.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCardsUseCase @Inject constructor(
    private val repository: CardRepository
) {
    operator fun invoke(
        query: String,
        deckId: Long? = null,
        labelIds: List<Long> = emptyList(),
        studyStatus: StudyStatus? = null
    ): Flow<List<Card>> = repository.searchCards(query, deckId, labelIds, studyStatus)
}
