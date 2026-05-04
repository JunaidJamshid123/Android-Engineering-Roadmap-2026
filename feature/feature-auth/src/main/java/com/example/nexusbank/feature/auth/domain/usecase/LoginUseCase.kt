package com.example.nexusbank.feature.auth.domain.usecase

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.LoginResponseData
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, password: String, mpin: String): Resource<LoginResponseData> {
        if (phone.isBlank()) return Resource.Error("Phone number is required")
        if (password.isBlank()) return Resource.Error("Password is required")
        if (mpin.length != 4) return Resource.Error("MPIN must be 4 digits")
        return authRepository.login(phone, password, mpin)
    }
}
