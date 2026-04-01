package com.wordbook.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wordbook.data.database.daos.CardDao
import com.wordbook.data.database.daos.DeckDao
import com.wordbook.data.database.daos.LabelDao
import com.wordbook.data.database.daos.TestDao
import com.wordbook.data.database.entities.*

@Database(
    entities = [
        DeckEntity::class,
        CardEntity::class,
        LabelEntity::class,
        DeckLabelEntity::class,
        CardLabelEntity::class,
        TestSessionEntity::class,
        TestResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WordBookDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun labelDao(): LabelDao
    abstract fun testDao(): TestDao
}
