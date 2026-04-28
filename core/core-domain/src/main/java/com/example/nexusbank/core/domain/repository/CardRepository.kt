package com.example.nexusbank.core.domain.repository

import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCards(): Flow<Resource<List<Card>>>
    fun getCardById(cardId: String): Flow<Resource<Card>>
    suspend fun lockCard(cardId: String, isLocked: Boolean): Resource<Card>
    suspend fun toggleOnline(cardId: String, isEnabled: Boolean): Resource<Card>
    suspend fun toggleInternational(cardId: String, isEnabled: Boolean): Resource<Card>
}
