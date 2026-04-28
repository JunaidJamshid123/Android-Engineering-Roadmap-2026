package com.example.nexusbank.feature.cards.data.mapper

import com.example.nexusbank.core.database.entity.CardEntity
import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.domain.model.CardNetwork
import com.example.nexusbank.core.domain.model.CardStatus
import com.example.nexusbank.core.domain.model.CardType
import com.example.nexusbank.core.network.model.CardDto

fun CardDto.toEntity(): CardEntity = CardEntity(
    id = id, userId = userId, cardNumber = cardNumber, type = type,
    network = network, expiryMonth = expiryMonth, expiryYear = expiryYear,
    nameOnCard = nameOnCard, isLocked = isLocked, isOnlineEnabled = isOnlineEnabled,
    isInternationalEnabled = isIntlEnabled, dailyLimit = dailyLimit, status = cardStatus
)

fun CardEntity.toDomain(): Card = Card(
    id = id, userId = userId, cardNumber = cardNumber,
    type = try { CardType.valueOf(type) } catch (_: Exception) { CardType.DEBIT },
    network = try { CardNetwork.valueOf(network) } catch (_: Exception) { CardNetwork.VISA },
    expiryMonth = expiryMonth, expiryYear = expiryYear,
    nameOnCard = nameOnCard, isLocked = isLocked, isOnlineEnabled = isOnlineEnabled,
    isInternationalEnabled = isInternationalEnabled, dailyLimit = dailyLimit,
    status = try { CardStatus.valueOf(status) } catch (_: Exception) { CardStatus.ACTIVE }
)
