package com.example.nexusbank.feature.transfers.di

import com.example.nexusbank.feature.transfers.data.TransferRepositoryImpl
import com.example.nexusbank.feature.transfers.domain.TransferRepository
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
    abstract fun bindTransferRepository(impl: TransferRepositoryImpl): TransferRepository
}
