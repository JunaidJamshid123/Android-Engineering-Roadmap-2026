package com.example.nexusbank.feature.accounts.di

import com.example.nexusbank.core.domain.repository.AccountRepository
import com.example.nexusbank.feature.accounts.data.repository.AccountRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountsModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository
}
