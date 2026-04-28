package com.example.nexusbank.feature.auth.domain.usecase

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, otp: String, otpRef: String): Resource<String> {
        if (otp.length != 6) return Resource.Error("OTP must be 6 digits")
        return authRepository.verifyOtp(phone, otp, otpRef)
    }
}
