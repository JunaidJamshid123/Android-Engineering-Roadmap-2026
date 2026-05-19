package com.example.nexusbank.feature.profile.data.repository

import com.example.nexusbank.core.database.dao.UserDao
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.ProfileUpdateData
import com.example.nexusbank.core.domain.repository.ProfileUpdateResult
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.NexusBankApiService
import com.example.nexusbank.core.network.model.UpdateProfilePictureRequest
import com.example.nexusbank.core.network.model.UpdateProfileRequest
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.feature.profile.data.mapper.toDomain
import com.example.nexusbank.feature.profile.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: NexusBankApiService,
    private val userDao: UserDao
) : UserRepository {

    override fun getUser(): Flow<Resource<User>> {
        return userDao.getUser().map { entity ->
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                Resource.Error("No cached profile")
            }
        }
    }

    override suspend fun refreshProfile(): Resource<User> {
        return when (val result = safeApiCall { apiService.getProfile() }) {
            is NetworkResult.Success -> {
                val envelope = result.data
                val dto = envelope.data
                if (envelope.success && dto != null) {
                    userDao.insertUser(dto.toEntity())
                    Resource.Success(dto.toDomain())
                } else {
                    Resource.Error(envelope.message ?: "Failed to load profile")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun updateProfile(request: ProfileUpdateData): Resource<ProfileUpdateResult> {
        val apiRequest = UpdateProfileRequest(
            fullName = request.fullName,
            email = request.email,
            dateOfBirth = request.dateOfBirth,
            gender = request.gender,
            fatherName = request.fatherName,
            cnic = request.cnic,
            maritalStatus = request.maritalStatus,
            nationality = request.nationality,
            occupation = request.occupation,
            monthlyIncome = request.monthlyIncome,
            addressLine = request.addressLine,
            city = request.city,
            country = request.country,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone
        )
        return when (val result = safeApiCall { apiService.updateProfile(apiRequest) }) {
            is NetworkResult.Success -> {
                val envelope = result.data
                val dto = envelope.data
                if (envelope.success && dto != null) {
                    userDao.insertUser(dto.toEntity())
                    Resource.Success(
                        ProfileUpdateResult(
                            message = envelope.message,
                            user = dto.toDomain()
                        )
                    )
                } else {
                    Resource.Error(envelope.message ?: "Failed to update profile")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun updateProfilePicture(pictureUrl: String): Resource<User> {
        val request = UpdateProfilePictureRequest(profilePictureUrl = pictureUrl)
        return when (val result = safeApiCall { apiService.updateProfilePicture(request) }) {
            is NetworkResult.Success -> {
                val envelope = result.data
                val dto = envelope.data
                if (envelope.success && dto != null) {
                    userDao.insertUser(dto.toEntity())
                    Resource.Success(dto.toDomain())
                } else {
                    Resource.Error(envelope.message ?: "Failed to update picture")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }
}
