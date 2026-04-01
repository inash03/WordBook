package com.wordbook.domain.repository

import com.wordbook.domain.model.Deck
import kotlinx.coroutines.flow.Flow

interface DeckRepository {
    fun getDecks(): Flow<List<Deck>>
    fun searchDecks(query: String, labelIds: List<Long>): Flow<List<Deck>>
    suspend fun getDeckById(id: Long): Deck?
    suspend fun insertDeck(deck: Deck): Long
    suspend fun updateDeck(deck: Deck)
    suspend fun deleteDeck(id: Long)
    suspend fun duplicateDeck(id: Long): Long
    suspend fun updateDeckLabels(deckId: Long, labelIds: List<Long>)
}
