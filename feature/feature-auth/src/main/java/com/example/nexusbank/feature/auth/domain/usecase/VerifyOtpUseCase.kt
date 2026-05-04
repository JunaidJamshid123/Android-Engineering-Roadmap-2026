package com.example.nexusbank.feature.auth.domain.usecase

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.MeResponseData
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<MeResponseData> {
        return authRepository.getMe()
    }
}
