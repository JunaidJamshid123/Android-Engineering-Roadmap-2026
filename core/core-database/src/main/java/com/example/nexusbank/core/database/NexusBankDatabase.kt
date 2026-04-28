package com.example.nexusbank.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nexusbank.core.database.dao.*
import com.example.nexusbank.core.database.entity.*

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        CardEntity::class,
        BeneficiaryEntity::class,
        LoanEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class NexusBankDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun cardDao(): CardDao
    abstract fun beneficiaryDao(): BeneficiaryDao
    abstract fun loanDao(): LoanDao
    abstract fun notificationDao(): NotificationDao
}
