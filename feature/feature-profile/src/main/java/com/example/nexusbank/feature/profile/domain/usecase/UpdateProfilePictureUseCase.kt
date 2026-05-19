package com.example.nexusbank.feature.profile.domain.usecase

import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
import javax.inject.Inject

class UpdateProfilePictureUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    /**
     * @param pictureUrl Either a hosted https URL or a Base64 data URI
     *                   in the form `data:image/jpeg;base64,...`.
     */
    suspend operator fun invoke(pictureUrl: String): Resource<User> {
        if (pictureUrl.isBlank()) return Resource.Error("Picture URL is required")
        return userRepository.updateProfilePicture(pictureUrl)
    }
}
