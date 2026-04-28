package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val avatarUrl: String?,
    val kycStatus: String,
    val createdAt: Long,
    val lastSynced: Long = System.currentTimeMillis()
)
