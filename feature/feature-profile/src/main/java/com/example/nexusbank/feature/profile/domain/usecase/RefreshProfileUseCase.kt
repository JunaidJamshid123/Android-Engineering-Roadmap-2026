package com.example.nexusbank.feature.profile.domain.usecase

import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
import javax.inject.Inject

class RefreshProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Resource<User> = userRepository.refreshProfile()
}
