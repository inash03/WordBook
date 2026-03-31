package com.wordbook.di

import com.wordbook.data.repository.CardRepositoryImpl
import com.wordbook.data.repository.DeckRepositoryImpl
import com.wordbook.data.repository.LabelRepositoryImpl
import com.wordbook.data.repository.TestRepositoryImpl
import com.wordbook.domain.repository.CardRepository
import com.wordbook.domain.repository.DeckRepository
import com.wordbook.domain.repository.LabelRepository
import com.wordbook.domain.repository.TestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindDeckRepository(impl: DeckRepositoryImpl): DeckRepository

    @Binds @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds @Singleton
    abstract fun bindLabelRepository(impl: LabelRepositoryImpl): LabelRepository

    @Binds @Singleton
    abstract fun bindTestRepository(impl: TestRepositoryImpl): TestRepository
}
