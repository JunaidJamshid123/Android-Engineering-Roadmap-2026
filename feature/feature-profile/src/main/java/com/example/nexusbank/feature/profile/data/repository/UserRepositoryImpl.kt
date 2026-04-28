package com.example.nexusbank.feature.profile.data.repository

import com.example.nexusbank.core.database.dao.UserDao
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.NexusBankApiService
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
                Resource.Error("User not found")
            }
        }
    }

    override suspend fun updateProfile(name: String, email: String): Resource<User> {
        return when (val result = safeApiCall { apiService.updateProfile(UpdateProfileRequest(name, email)) }) {
            is NetworkResult.Success -> {
                val userDto = result.data.user
                userDao.insertUser(userDto.toEntity())
                Resource.Success(userDto.toDomain())
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }
}
