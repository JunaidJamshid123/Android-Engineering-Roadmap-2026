package com.example.nexusbank.core.domain.repository

import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(): Flow<Resource<User>>
    suspend fun updateProfile(name: String, email: String): Resource<User>
}
