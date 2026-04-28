package com.example.nexusbank.feature.transfers.di

import com.example.nexusbank.core.domain.repository.TransactionRepository
import com.example.nexusbank.feature.transfers.data.repository.BeneficiaryRepositoryImpl
import com.example.nexusbank.feature.transfers.data.repository.TransactionRepositoryImpl
import com.example.nexusbank.feature.transfers.domain.repository.BeneficiaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransfersModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBeneficiaryRepository(impl: BeneficiaryRepositoryImpl): BeneficiaryRepository
}
