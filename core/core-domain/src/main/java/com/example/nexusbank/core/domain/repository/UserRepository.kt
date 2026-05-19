package com.example.nexusbank.core.domain.repository

import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<Resource<User>>
    suspend fun refreshProfile(): Resource<User>
    suspend fun updateProfile(request: ProfileUpdateData): Resource<ProfileUpdateResult>
    suspend fun updateProfilePicture(pictureUrl: String): Resource<User>
}

data class ProfileUpdateResult(
    val message: String?,
    val user: User
)

data class ProfileUpdateData(
    val fullName: String? = null,
    val email: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val fatherName: String? = null,
    val cnic: String? = null,
    val maritalStatus: String? = null,
    val nationality: String? = null,
    val occupation: String? = null,
    val monthlyIncome: Long? = null,
    val addressLine: String? = null,
    val city: String? = null,
    val country: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null
)
