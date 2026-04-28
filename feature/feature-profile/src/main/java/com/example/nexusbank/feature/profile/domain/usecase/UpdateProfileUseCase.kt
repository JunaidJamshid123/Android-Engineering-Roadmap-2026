package com.example.nexusbank.feature.profile.domain.usecase

import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String): Resource<User> {
        if (name.isBlank()) return Resource.Error("Name is required")
        if (email.isBlank()) return Resource.Error("Email is required")
        return userRepository.updateProfile(name, email)
    }
}
