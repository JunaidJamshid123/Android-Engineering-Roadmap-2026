package com.example.nexusbank.feature.cards.domain.usecase

import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.domain.repository.CardRepository
import com.example.nexusbank.core.domain.util.Resource
import javax.inject.Inject

class ToggleCardLockUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(cardId: String, lock: Boolean): Resource<Card> =
        cardRepository.lockCard(cardId, lock)
}
