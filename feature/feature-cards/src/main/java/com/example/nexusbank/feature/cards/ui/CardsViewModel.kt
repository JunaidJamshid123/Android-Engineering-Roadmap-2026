package com.example.nexusbank.feature.cards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.cards.domain.usecase.GetCardsUseCase
import com.example.nexusbank.feature.cards.domain.usecase.ToggleCardLockUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CardsUiState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val toggleCardLockUseCase: ToggleCardLockUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init { loadCards() }

    fun loadCards() {
        viewModelScope.launch {
            getCardsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, cards = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun toggleLock(cardId: String, lock: Boolean) {
        viewModelScope.launch {
            when (val result = toggleCardLockUseCase(cardId, lock)) {
                is Resource.Success -> loadCards()
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> { }
            }
        }
    }
}
