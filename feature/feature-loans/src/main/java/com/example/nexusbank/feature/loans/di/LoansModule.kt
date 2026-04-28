package com.example.nexusbank.feature.loans.di

import com.example.nexusbank.feature.loans.data.repository.LoanRepositoryImpl
import com.example.nexusbank.feature.loans.domain.repository.LoanRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoansModule {
    @Binds
    @Singleton
    abstract fun bindLoanRepository(impl: LoanRepositoryImpl): LoanRepository
}
