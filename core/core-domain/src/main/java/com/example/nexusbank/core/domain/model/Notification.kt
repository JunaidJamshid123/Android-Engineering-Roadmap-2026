package com.example.nexusbank.core.domain.model

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val isRead: Boolean,
    val deepLink: String?,
    val createdAt: Long
)

enum class NotificationType {
    TRANSACTION, PAYMENT_DUE, OFFER, SECURITY, GENERAL
}
