package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
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
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val accountNumber: String,
    val type: String, // SAVINGS, CURRENT, FIXED_DEPOSIT, RECURRING_DEPOSIT
    val balance: Double,
    val currency: String,
    val branchName: String,
    val ifscCode: String,
    val isActive: Boolean,
    val lastSynced: Long = System.currentTimeMillis()
)
