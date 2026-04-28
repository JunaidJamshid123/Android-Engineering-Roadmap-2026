package com.example.nexusbank.feature.notifications.data.mapper

import com.example.nexusbank.core.database.entity.NotificationEntity
import com.example.nexusbank.core.domain.model.Notification
import com.example.nexusbank.core.domain.model.NotificationType

fun NotificationEntity.toDomain(): Notification = Notification(
    id = id, userId = userId, title = title, body = body,
    type = try { NotificationType.valueOf(type) } catch (_: Exception) { NotificationType.GENERAL },
    isRead = isRead, deepLink = deepLink, createdAt = createdAt
)
