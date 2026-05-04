package com.example.nexusbank.feature.auth.domain.repository

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.LoginResponseData
import com.example.nexusbank.core.network.model.MeResponseData
import com.example.nexusbank.core.network.model.RegisterResponseData

interface AuthRepository {
    suspend fun register(
        phone: String,
        fullName: String,
        email: String,
        dateOfBirth: String,
        gender: String,
        password: String,
        mpin: String
    ): Resource<RegisterResponseData>

    suspend fun login(
        phone: String,
        password: String,
        mpin: String
    ): Resource<LoginResponseData>

    suspend fun checkPhone(phone: String): Resource<Boolean>

    suspend fun getMe(): Resource<MeResponseData>

    suspend fun logout(): Resource<Unit>

    fun isLoggedIn(): Boolean
}
