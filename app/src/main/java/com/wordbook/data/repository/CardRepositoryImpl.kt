package com.wordbook.data.repository

import com.wordbook.data.database.daos.CardDao
import com.wordbook.data.database.entities.CardLabelEntity
import com.wordbook.domain.model.Card
import com.wordbook.domain.model.StudyStatus
import com.wordbook.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val cardDao: CardDao
) : CardRepository {

    override fun getCardsForDeck(deckId: Long): Flow<List<Card>> =
        cardDao.getCardsForDeck(deckId).map { list -> list.map { it.toDomain() } }

    override fun searchCards(
        query: String,
        deckId: Long?,
        labelIds: List<Long>,
        studyStatus: StudyStatus?
    ): Flow<List<Card>> = cardDao.searchCards(
        query = query,
        deckId = deckId,
        labelIds = labelIds,
        hasLabelFilter = if (labelIds.isEmpty()) 0 else 1,
        studyStatus = studyStatus?.name
    ).map { list -> list.map { it.toDomain() } }

    override fun getDueCards(deckId: Long, dueDate: Long): Flow<List<Card>> =
        cardDao.getDueCards(deckId, dueDate).map { list -> list.map { it.toDomain() } }

    override suspend fun getCardById(id: Long): Card? =
        cardDao.getCardWithLabelsById(id)?.toDomain()

    override suspend fun insertCard(card: Card): Long =
        cardDao.insertCard(card.toEntity())

    override suspend fun updateCard(card: Card) =
        cardDao.updateCard(card.toEntity())

    override suspend fun deleteCard(id: Long) =
        cardDao.deleteCard(id)

    override suspend fun deleteCards(ids: List<Long>) =
        cardDao.deleteCards(ids)

    override suspend fun updateCardStudyStatus(id: Long, status: StudyStatus) =
        cardDao.updateStudyStatus(id, status.name)

    override suspend fun updateCardSrs(
        id: Long,
        interval: Int,
        easeFactor: Float,
        dueDate: Long,
        repetitions: Int
    ) = cardDao.updateSrs(id, interval, easeFactor, dueDate, repetitions)

    override suspend fun moveCards(cardIds: List<Long>, targetDeckId: Long) =
        cardDao.moveCards(cardIds, targetDeckId)

    override suspend fun updateCardLabels(cardId: Long, labelIds: List<Long>) {
        cardDao.deleteCardLabels(cardId)
        if (labelIds.isNotEmpty()) {
            cardDao.insertCardLabels(labelIds.map { CardLabelEntity(cardId, it) })
        }
    }
}
