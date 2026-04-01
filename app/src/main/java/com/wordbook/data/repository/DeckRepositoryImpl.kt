package com.wordbook.data.repository

import com.wordbook.data.database.daos.DeckDao
import com.wordbook.data.database.entities.DeckLabelEntity
import com.wordbook.domain.model.Deck
import com.wordbook.domain.repository.DeckRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeckRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao
) : DeckRepository {

    override fun getDecks(): Flow<List<Deck>> =
        deckDao.getAllDecksWithLabels().map { list ->
            list.map { it.toDomainWithCounts() }
        }

    override fun searchDecks(query: String, labelIds: List<Long>): Flow<List<Deck>> =
        deckDao.searchDecksWithLabels(query, labelIds, if (labelIds.isEmpty()) 0 else 1).map { list ->
            list.map { it.toDomainWithCounts() }
        }

    override suspend fun getDeckById(id: Long): Deck? =
        deckDao.getDeckWithLabelsById(id)?.toDomainWithCounts()

    override suspend fun insertDeck(deck: Deck): Long =
        deckDao.insertDeck(deck.toEntity())

    override suspend fun updateDeck(deck: Deck) =
        deckDao.updateDeck(deck.toEntity())

    override suspend fun deleteDeck(id: Long) =
        deckDao.deleteDeck(id)

    override suspend fun duplicateDeck(id: Long): Long {
        val original = deckDao.getDeckWithLabelsById(id) ?: return -1L
        val now = System.currentTimeMillis()
        val newDeck = original.deck.copy(
            id = 0,
            name = "${original.deck.name} (Copy)",
            createdAt = now,
            updatedAt = now
        )
        val newId = deckDao.insertDeck(newDeck)
        if (original.labels.isNotEmpty()) {
            deckDao.insertDeckLabels(original.labels.map { DeckLabelEntity(newId, it.id) })
        }
        return newId
    }

    override suspend fun updateDeckLabels(deckId: Long, labelIds: List<Long>) {
        deckDao.deleteDeckLabels(deckId)
        if (labelIds.isNotEmpty()) {
            deckDao.insertDeckLabels(labelIds.map { DeckLabelEntity(deckId, it) })
        }
    }

    private suspend fun com.wordbook.data.database.daos.DeckWithLabels.toDomainWithCounts(): Deck {
        val now = System.currentTimeMillis()
        return toDomain(
            cardCount = deckDao.getCardCount(deck.id),
            rememberedCount = deckDao.getRememberedCount(deck.id),
            needsReviewCount = deckDao.getNeedsReviewCount(deck.id),
            dueCardsCount = deckDao.getDueCardsCount(deck.id, now)
        )
    }
}
