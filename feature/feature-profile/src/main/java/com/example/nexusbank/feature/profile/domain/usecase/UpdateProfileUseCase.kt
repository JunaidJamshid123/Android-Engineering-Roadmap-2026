package com.example.nexusbank.feature.profile.domain.usecase

import com.example.nexusbank.core.domain.repository.ProfileUpdateData
import com.example.nexusbank.core.domain.repository.ProfileUpdateResult
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(data: ProfileUpdateData): Resource<ProfileUpdateResult> {
        return userRepository.updateProfile(data)
    }
}
