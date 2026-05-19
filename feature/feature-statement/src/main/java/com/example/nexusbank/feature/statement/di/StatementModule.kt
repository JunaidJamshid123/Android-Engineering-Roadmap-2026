package com.example.nexusbank.feature.statement.di

import com.example.nexusbank.core.network.di.Authenticated
import com.example.nexusbank.feature.statement.data.api.StatementApiService
import com.example.nexusbank.feature.statement.data.repository.StatementRepositoryImpl
import com.example.nexusbank.feature.statement.domain.repository.StatementRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StatementApiModule {

    @Provides
    @Singleton
    fun provideStatementApiService(@Authenticated retrofit: Retrofit): StatementApiService =
        retrofit.create(StatementApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StatementBindsModule {

    @Binds
    @Singleton
    abstract fun bindStatementRepository(
        impl: StatementRepositoryImpl
    ): StatementRepository
}
