package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class CardEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val cardNumber: String,
    val type: String, // DEBIT, CREDIT
    val network: String, // VISA, MASTERCARD, RUPAY
    val expiryMonth: Int,
    val expiryYear: Int,
    val nameOnCard: String,
    val isLocked: Boolean,
    val isOnlineEnabled: Boolean,
    val isInternationalEnabled: Boolean,
    val dailyLimit: Double,
    val status: String, // ACTIVE, BLOCKED, EXPIRED
    val lastSynced: Long = System.currentTimeMillis()
)
