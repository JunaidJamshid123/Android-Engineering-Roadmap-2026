package com.example.nexusbank.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val fullName: String,
    val phone: String,
    val email: String,
    val dateOfBirth: String?,
    val gender: String?,
    val kycStatus: String,
    val fatherName: String?,
    val cnic: String?,
    val maritalStatus: String?,
    val nationality: String?,
    val occupation: String?,
    val monthlyIncome: String?,
    val addressLine: String?,
    val city: String?,
    val country: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val profilePictureUrl: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastSynced: Long = System.currentTimeMillis()
)
