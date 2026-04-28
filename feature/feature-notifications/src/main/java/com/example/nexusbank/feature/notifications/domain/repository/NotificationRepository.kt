package com.example.nexusbank.feature.notifications.domain.repository

import com.example.nexusbank.core.domain.model.Notification
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<Resource<List<Notification>>>
    fun getUnreadCount(userId: String): Flow<Int>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
    suspend fun markAllAsRead(userId: String): Resource<Unit>
}
