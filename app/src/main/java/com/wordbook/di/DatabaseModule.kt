package com.wordbook.di

import android.content.Context
import androidx.room.Room
import com.wordbook.data.database.WordBookDatabase
import com.wordbook.data.database.daos.CardDao
import com.wordbook.data.database.daos.DeckDao
import com.wordbook.data.database.daos.LabelDao
import com.wordbook.data.database.daos.TestDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WordBookDatabase =
        Room.databaseBuilder(context, WordBookDatabase::class.java, "wordbook.db")
            .build()

    @Provides fun provideDeckDao(db: WordBookDatabase): DeckDao = db.deckDao()
    @Provides fun provideCardDao(db: WordBookDatabase): CardDao = db.cardDao()
    @Provides fun provideLabelDao(db: WordBookDatabase): LabelDao = db.labelDao()
    @Provides fun provideTestDao(db: WordBookDatabase): TestDao = db.testDao()
}
