package com.example.nexusbank.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.nexusbank.core.database.NexusBankDatabase
import com.example.nexusbank.core.database.dao.*
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
    fun provideDatabase(@ApplicationContext context: Context): NexusBankDatabase {
        return Room.databaseBuilder(
            context,
            NexusBankDatabase::class.java,
            "nexus_bank.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: NexusBankDatabase): UserDao = database.userDao()

    @Provides
    fun provideAccountDao(database: NexusBankDatabase): AccountDao = database.accountDao()

    @Provides
    fun provideTransactionDao(database: NexusBankDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideCardDao(database: NexusBankDatabase): CardDao = database.cardDao()

    @Provides
    fun provideBeneficiaryDao(database: NexusBankDatabase): BeneficiaryDao = database.beneficiaryDao()

    @Provides
    fun provideLoanDao(database: NexusBankDatabase): LoanDao = database.loanDao()

    @Provides
    fun provideNotificationDao(database: NexusBankDatabase): NotificationDao = database.notificationDao()
}
