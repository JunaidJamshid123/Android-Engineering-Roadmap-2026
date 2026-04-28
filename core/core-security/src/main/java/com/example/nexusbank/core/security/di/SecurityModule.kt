package com.example.nexusbank.core.security.di

import android.content.Context
import com.example.nexusbank.core.network.interceptor.TokenProvider
import com.example.nexusbank.core.security.EncryptedPrefs
import com.example.nexusbank.core.security.SecurityManager
import com.example.nexusbank.core.security.TokenProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideEncryptedPrefs(
        @ApplicationContext context: Context
    ): EncryptedPrefs = EncryptedPrefs(context)

    @Provides
    @Singleton
    fun provideSecurityManager(
        encryptedPrefs: EncryptedPrefs
    ): SecurityManager = SecurityManager(encryptedPrefs)

    @Provides
    @Singleton
    fun provideTokenProvider(
        encryptedPrefs: EncryptedPrefs
    ): TokenProvider = TokenProviderImpl(encryptedPrefs)
}
