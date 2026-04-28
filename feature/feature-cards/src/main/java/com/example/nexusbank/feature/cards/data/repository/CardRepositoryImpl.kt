package com.example.nexusbank.feature.cards.data.repository

import com.example.nexusbank.core.database.dao.CardDao
import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.domain.repository.CardRepository
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.NexusBankApiService
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.feature.cards.data.mapper.toDomain
import com.example.nexusbank.feature.cards.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepositoryImpl @Inject constructor(
    private val apiService: NexusBankApiService,
    private val cardDao: CardDao
) : CardRepository {

    override fun getCards(): Flow<Resource<List<Card>>> {
        return cardDao.getAllCards().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getCardById(cardId: String): Flow<Resource<Card>> {
        return cardDao.getCardById(cardId).map { entity ->
            if (entity != null) Resource.Success(entity.toDomain())
            else Resource.Error("Card not found")
        }
    }

    override suspend fun lockCard(cardId: String, isLocked: Boolean): Resource<Card> {
        return when (val result = safeApiCall { apiService.lockCard(cardId, mapOf("locked" to isLocked)) }) {
            is NetworkResult.Success -> {
                val entity = result.data.card.toEntity()
                cardDao.updateCard(entity)
                Resource.Success(entity.toDomain())
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun toggleOnline(cardId: String, isEnabled: Boolean): Resource<Card> {
        return when (val result = safeApiCall { apiService.toggleOnline(cardId, mapOf("enabled" to isEnabled)) }) {
            is NetworkResult.Success -> {
                val entity = result.data.card.toEntity()
                cardDao.updateCard(entity)
                Resource.Success(entity.toDomain())
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun toggleInternational(cardId: String, isEnabled: Boolean): Resource<Card> {
        return when (val result = safeApiCall { apiService.toggleInternational(cardId, mapOf("enabled" to isEnabled)) }) {
            is NetworkResult.Success -> {
                val entity = result.data.card.toEntity()
                cardDao.updateCard(entity)
                Resource.Success(entity.toDomain())
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }
}
