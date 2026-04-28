package com.example.nexusbank.feature.loans.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.model.Loan
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.security.EncryptedPrefs
import com.example.nexusbank.feature.loans.domain.usecase.GetLoansUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoansUiState(
    val loans: List<Loan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val getLoansUseCase: GetLoansUseCase,
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoansUiState())
    val uiState: StateFlow<LoansUiState> = _uiState.asStateFlow()

    init { loadLoans() }

    fun loadLoans() {
        val userId = encryptedPrefs.userId ?: return
        viewModelScope.launch {
            getLoansUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, loans = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
