package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "loans",
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
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: String, // PERSONAL, HOME, AUTO
    val principalAmount: Double,
    val outstandingAmount: Double,
    val interestRate: Double,
    val emiAmount: Double,
    val tenureMonths: Int,
    val startDate: Long,
    val endDate: Long,
    val nextEmiDate: Long?,
    val status: String, // ACTIVE, CLOSED, DEFAULT
    val lastSynced: Long = System.currentTimeMillis()
)
