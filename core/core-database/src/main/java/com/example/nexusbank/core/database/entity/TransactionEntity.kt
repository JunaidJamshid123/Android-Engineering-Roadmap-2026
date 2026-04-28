package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("accountId"), Index("timestamp")]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val accountId: String,
    val type: String, // CREDIT, DEBIT
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String?,
    val referenceId: String,
    val status: String, // SUCCESS, PENDING, FAILED
    val recipientName: String?,
    val recipientAccount: String?,
    val mode: String?, // UPI, NEFT, IMPS, RTGS, INTERNAL
    val timestamp: Long,
    val lastSynced: Long = System.currentTimeMillis()
)
