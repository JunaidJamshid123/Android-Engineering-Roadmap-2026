package com.example.nexusbank.feature.notifications.domain.usecase

import com.example.nexusbank.core.domain.model.Notification
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<Notification>>> =
        notificationRepository.getNotifications(userId)
}
