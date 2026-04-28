package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
    val id: String,
    val userId: String,
    val cardNumber: String,
    val type: String,
    val network: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val nameOnCard: String,
    val isLocked: Boolean,
    val isOnlineEnabled: Boolean,
    val isIntlEnabled: Boolean,
    val dailyLimit: Double,
    val cardStatus: String
)

@Serializable
data class CardsResponse(
    val cards: List<CardDto>
)

@Serializable
data class CardResponse(
    val card: CardDto
)
