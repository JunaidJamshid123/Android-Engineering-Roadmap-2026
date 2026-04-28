package com.example.nexusbank.feature.cards.di

import com.example.nexusbank.core.domain.repository.CardRepository
import com.example.nexusbank.feature.cards.data.repository.CardRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CardsModule {

    @Binds
    @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository
}
