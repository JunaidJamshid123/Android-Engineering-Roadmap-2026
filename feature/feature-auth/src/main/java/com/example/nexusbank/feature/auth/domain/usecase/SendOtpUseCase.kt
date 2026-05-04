package com.example.nexusbank.feature.auth.domain.usecase

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Resource<Boolean> {
        if (phone.isBlank()) return Resource.Error("Phone number is required")
        return authRepository.checkPhone(phone)
    }
}
