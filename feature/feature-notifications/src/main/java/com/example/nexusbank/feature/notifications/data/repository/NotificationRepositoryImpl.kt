package com.example.nexusbank.feature.notifications.data.repository

import com.example.nexusbank.core.database.dao.NotificationDao
import com.example.nexusbank.core.domain.model.Notification
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.notifications.data.mapper.toDomain
import com.example.nexusbank.feature.notifications.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<Notification>>> {
        return notificationDao.getNotifications(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = notificationDao.getUnreadCount(userId)

    override suspend fun markAsRead(notificationId: String): Resource<Unit> {
        notificationDao.markAsRead(notificationId)
        return Resource.Success(Unit)
    }

    override suspend fun markAllAsRead(userId: String): Resource<Unit> {
        notificationDao.markAllAsRead(userId)
        return Resource.Success(Unit)
    }
}
