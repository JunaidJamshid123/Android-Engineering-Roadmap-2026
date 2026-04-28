package com.example.nexusbank.core.domain.model

data class Card(
    val id: String,
    val userId: String,
    val cardNumber: String, // masked: **** **** **** 1234
    val type: CardType,
    val network: CardNetwork,
    val expiryMonth: Int,
    val expiryYear: Int,
    val nameOnCard: String,
    val isLocked: Boolean,
    val isOnlineEnabled: Boolean,
    val isInternationalEnabled: Boolean,
    val dailyLimit: Double,
    val status: CardStatus
)

enum class CardType { DEBIT, CREDIT }

enum class CardNetwork { VISA, MASTERCARD, RUPAY }

enum class CardStatus { ACTIVE, BLOCKED, EXPIRED }
