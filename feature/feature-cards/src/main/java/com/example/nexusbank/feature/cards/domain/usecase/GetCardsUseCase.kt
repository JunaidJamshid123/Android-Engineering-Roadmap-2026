package com.example.nexusbank.feature.cards.domain.usecase

import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.domain.repository.CardRepository
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(): Flow<Resource<List<Card>>> = cardRepository.getCards()
}
