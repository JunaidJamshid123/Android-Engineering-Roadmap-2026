package com.example.nexusbank.feature.transactions.di

import com.example.nexusbank.feature.transactions.data.TransactionsRepositoryImpl
import com.example.nexusbank.feature.transactions.domain.TransactionsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionsModule {

    @Binds
    @Singleton
    abstract fun bindTransactionsRepository(
        impl: TransactionsRepositoryImpl
    ): TransactionsRepository
}
