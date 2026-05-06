package com.example.nexusbank.feature.transfers.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.BankAccountDto
import com.example.nexusbank.core.network.model.ResolveRecipientData
import com.example.nexusbank.feature.transfers.domain.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewTransferUiState(
    val accounts: List<BankAccountDto> = emptyList(),
    val selectedFromAccountId: String? = null,
    val toAccountNumber: String = "",
    val isLoadingAccounts: Boolean = false,
    val isResolving: Boolean = false,
    val resolved: ResolveRecipientData? = null,
    val error: String? = null
) {
    val selectedAccount: BankAccountDto?
        get() = accounts.firstOrNull { it.id == selectedFromAccountId }

    val canContinue: Boolean
        get() = !isResolving &&
            selectedFromAccountId != null &&
            toAccountNumber.length in 10..25
}

@HiltViewModel
class NewTransferViewModel @Inject constructor(
    private val repository: TransferRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewTransferUiState())
    val state: StateFlow<NewTransferUiState> = _state.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        _state.update { it.copy(isLoadingAccounts = true, error = null) }
        viewModelScope.launch {
            when (val r = repository.getMyAccounts()) {
                is Resource.Success -> _state.update {
                    it.copy(
                        accounts = r.data,
                        selectedFromAccountId = it.selectedFromAccountId
                            ?: r.data.firstOrNull()?.id,
                        isLoadingAccounts = false
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoadingAccounts = false, error = r.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun selectFromAccount(id: String) {
        _state.update { it.copy(selectedFromAccountId = id) }
    }

    fun onToAccountNumberChange(value: String) {
        _state.update {
            it.copy(toAccountNumber = value.filter { c -> c.isLetterOrDigit() }.take(25))
        }
    }

    fun onContinueClick() {
        val current = _state.value
        if (!current.canContinue) return
        _state.update { it.copy(isResolving = true, error = null, resolved = null) }
        viewModelScope.launch {
            when (val r = repository.resolveRecipient(current.toAccountNumber)) {
                is Resource.Success -> _state.update {
                    it.copy(isResolving = false, resolved = r.data)
                }
                is Resource.Error -> _state.update {
                    it.copy(isResolving = false, error = r.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeResolved() {
        _state.update { it.copy(resolved = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
