package com.wordbook.data.database.daos

import androidx.room.*
import com.wordbook.data.database.entities.DeckEntity
import com.wordbook.data.database.entities.DeckLabelEntity
import com.wordbook.data.database.entities.LabelEntity
import kotlinx.coroutines.flow.Flow

data class DeckWithLabels(
    @Embedded val deck: DeckEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DeckLabelEntity::class,
            parentColumn = "deckId",
            entityColumn = "labelId"
        )
    )
    val labels: List<LabelEntity>
)

@Dao
interface DeckDao {

    @Transaction
    @Query("SELECT * FROM decks ORDER BY updatedAt DESC")
    fun getAllDecksWithLabels(): Flow<List<DeckWithLabels>>

    @Transaction
    @Query("""
        SELECT DISTINCT d.* FROM decks d
        LEFT JOIN deck_labels dl ON d.id = dl.deckId
        LEFT JOIN labels l ON dl.labelId = l.id
        WHERE (:query = '' OR d.name LIKE '%' || :query || '%')
        AND (:hasLabelFilter = 0 OR dl.labelId IN (:labelIds))
        ORDER BY d.updatedAt DESC
    """)
    fun searchDecksWithLabels(
        query: String,
        labelIds: List<Long>,
        hasLabelFilter: Int
    ): Flow<List<DeckWithLabels>>

    @Transaction
    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckWithLabelsById(id: Long): DeckWithLabels?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteDeck(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeckLabels(labels: List<DeckLabelEntity>)

    @Query("DELETE FROM deck_labels WHERE deckId = :deckId")
    suspend fun deleteDeckLabels(deckId: Long)

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId")
    suspend fun getCardCount(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND studyStatus = 'REMEMBERED'")
    suspend fun getRememberedCount(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND studyStatus = 'NEEDS_REVIEW'")
    suspend fun getNeedsReviewCount(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND srsDueDate <= :now AND srsRepetitions > 0")
    suspend fun getDueCardsCount(deckId: Long, now: Long): Int
}
