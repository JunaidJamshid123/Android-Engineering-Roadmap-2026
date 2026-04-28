package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "beneficiaries",
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
data class BeneficiaryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val accountNumber: String,
    val ifscCode: String,
    val bankName: String,
    val nickname: String?,
    val transferLimit: Double?,
    val isVerified: Boolean,
    val createdAt: Long
)
