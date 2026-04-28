package com.example.nexusbank.feature.auth.domain.usecase

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, mpin: String): Resource<Unit> {
        if (phone.isBlank()) return Resource.Error("Phone number is required")
        if (mpin.length != 4) return Resource.Error("MPIN must be 4 digits")
        return authRepository.loginWithMpin(phone, mpin)
    }
}
