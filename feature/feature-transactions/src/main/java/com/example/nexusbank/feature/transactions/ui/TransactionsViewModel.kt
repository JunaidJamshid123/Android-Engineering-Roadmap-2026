package com.example.nexusbank.feature.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.TransferHistoryItem
import com.example.nexusbank.feature.transactions.domain.TransactionsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val isLoading: Boolean = false,
    val items: List<TransferHistoryItem> = emptyList(),
    val total: Int = 0,
    val error: String? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionsUiState())
    val state: StateFlow<TransactionsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.getHistory(limit = 50, offset = 0)) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        items = result.data.items,
                        total = result.data.total
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
