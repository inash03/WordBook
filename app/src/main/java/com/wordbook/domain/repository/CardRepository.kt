package com.wordbook.domain.repository

import com.wordbook.domain.model.Card
import com.wordbook.domain.model.StudyStatus
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCardsForDeck(deckId: Long): Flow<List<Card>>
    fun searchCards(
        query: String,
        deckId: Long?,
        labelIds: List<Long>,
        studyStatus: StudyStatus?
    ): Flow<List<Card>>
    fun getDueCards(deckId: Long, dueDate: Long): Flow<List<Card>>
    suspend fun getCardById(id: Long): Card?
    suspend fun insertCard(card: Card): Long
    suspend fun updateCard(card: Card)
    suspend fun deleteCard(id: Long)
    suspend fun deleteCards(ids: List<Long>)
    suspend fun updateCardStudyStatus(id: Long, status: StudyStatus)
    suspend fun updateCardSrs(
        id: Long,
        interval: Int,
        easeFactor: Float,
        dueDate: Long,
        repetitions: Int
    )
    suspend fun moveCards(cardIds: List<Long>, targetDeckId: Long)
    suspend fun updateCardLabels(cardId: Long, labelIds: List<Long>)
}
