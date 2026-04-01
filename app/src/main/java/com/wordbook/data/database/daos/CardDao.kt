package com.wordbook.data.database.daos

import androidx.room.*
import com.wordbook.data.database.entities.CardEntity
import com.wordbook.data.database.entities.CardLabelEntity
import com.wordbook.data.database.entities.LabelEntity
import kotlinx.coroutines.flow.Flow

data class CardWithLabels(
    @Embedded val card: CardEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CardLabelEntity::class,
            parentColumn = "cardId",
            entityColumn = "labelId"
        )
    )
    val labels: List<LabelEntity>
)

@Dao
interface CardDao {

    @Transaction
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY createdAt ASC")
    fun getCardsForDeck(deckId: Long): Flow<List<CardWithLabels>>

    @Transaction
    @Query("""
        SELECT DISTINCT c.* FROM cards c
        LEFT JOIN card_labels cl ON c.id = cl.cardId
        LEFT JOIN labels l ON cl.labelId = l.id
        WHERE (:deckId IS NULL OR c.deckId = :deckId)
        AND (c.front LIKE '%' || :query || '%' OR c.back LIKE '%' || :query || '%')
        AND (:hasLabelFilter = 0 OR cl.labelId IN (:labelIds))
        AND (:studyStatus IS NULL OR c.studyStatus = :studyStatus)
        ORDER BY c.createdAt ASC
    """)
    fun searchCards(
        query: String,
        deckId: Long?,
        labelIds: List<Long>,
        hasLabelFilter: Int,
        studyStatus: String?
    ): Flow<List<CardWithLabels>>

    @Transaction
    @Query("SELECT * FROM cards WHERE deckId = :deckId AND srsDueDate <= :dueDate AND srsRepetitions > 0")
    fun getDueCards(deckId: Long, dueDate: Long): Flow<List<CardWithLabels>>

    @Transaction
    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardWithLabelsById(id: Long): CardWithLabels?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Query("DELETE FROM cards WHERE id IN (:ids)")
    suspend fun deleteCards(ids: List<Long>)

    @Query("UPDATE cards SET studyStatus = :status WHERE id = :id")
    suspend fun updateStudyStatus(id: Long, status: String)

    @Query("""
        UPDATE cards SET srsInterval = :interval, srsEaseFactor = :easeFactor,
        srsDueDate = :dueDate, srsRepetitions = :repetitions WHERE id = :id
    """)
    suspend fun updateSrs(id: Long, interval: Int, easeFactor: Float, dueDate: Long, repetitions: Int)

    @Query("UPDATE cards SET deckId = :targetDeckId WHERE id IN (:cardIds)")
    suspend fun moveCards(cardIds: List<Long>, targetDeckId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardLabels(labels: List<CardLabelEntity>)

    @Query("DELETE FROM card_labels WHERE cardId = :cardId")
    suspend fun deleteCardLabels(cardId: Long)

    @Transaction
    @Query("SELECT * FROM cards WHERE deckId = :deckId AND studyStatus = 'NEEDS_REVIEW' ORDER BY createdAt ASC")
    suspend fun getNeedsReviewCards(deckId: Long): List<CardWithLabels>

    @Transaction
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY RANDOM()")
    suspend fun getCardsRandom(deckId: Long): List<CardWithLabels>

    @Transaction
    @Query("SELECT * FROM cards WHERE deckId = :deckId ORDER BY createdAt ASC")
    suspend fun getCardsSequential(deckId: Long): List<CardWithLabels>

    @Transaction
    @Query("""
        SELECT * FROM cards WHERE deckId = :deckId
        ORDER BY CASE studyStatus WHEN 'NEEDS_REVIEW' THEN 0 ELSE 1 END, createdAt ASC
    """)
    suspend fun getCardsNeedsReviewFirst(deckId: Long): List<CardWithLabels>

    @Transaction
    @Query("SELECT * FROM cards WHERE deckId = :deckId AND srsDueDate <= :now AND srsRepetitions > 0 ORDER BY srsDueDate ASC")
    suspend fun getDueCardsForTest(deckId: Long, now: Long): List<CardWithLabels>
}
