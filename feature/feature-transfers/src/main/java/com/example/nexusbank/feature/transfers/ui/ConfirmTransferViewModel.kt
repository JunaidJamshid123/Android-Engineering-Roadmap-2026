package com.example.nexusbank.feature.transfers.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.TransferResponseData
import com.example.nexusbank.feature.transfers.domain.TransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

object TransferPurpose {
    const val FAMILY = "FAMILY"
    const val FRIENDS = "FRIENDS"
    const val BILLS = "BILLS"
    const val BUSINESS = "BUSINESS"
    const val EDUCATION = "EDUCATION"
    const val OTHER = "OTHER"
    val ALL = listOf(FAMILY, FRIENDS, BILLS, BUSINESS, EDUCATION, OTHER)
}

data class ConfirmTransferUiState(
    val fromAccountId: String = "",
    val toAccountNumber: String = "",
    val recipientName: String = "",
    val recipientAccountMasked: String = "",
    val recipientAccountType: String = "",
    val amount: String = "",
    val purpose: String = "",
    val remarks: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val success: TransferResponseData? = null
) {
    val amountValue: Double
        get() = amount.toDoubleOrNull() ?: 0.0

    val canSend: Boolean
        get() = !isSending && amountValue > 0.0 && purpose.isNotBlank() &&
            fromAccountId.isNotBlank() && toAccountNumber.isNotBlank()
}

@HiltViewModel
class ConfirmTransferViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TransferRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ConfirmTransferUiState(
            fromAccountId = savedStateHandle["fromAccountId"] ?: "",
            toAccountNumber = savedStateHandle["toAccountNumber"] ?: "",
            recipientName = savedStateHandle["holderName"] ?: "",
            recipientAccountMasked = savedStateHandle["accountMasked"] ?: "",
            recipientAccountType = savedStateHandle["accountType"] ?: ""
        )
    )
    val state: StateFlow<ConfirmTransferUiState> = _state.asStateFlow()

    fun onAmountChange(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        val onlyOneDot = sanitized.indexOf('.').let { idx ->
            if (idx < 0) sanitized
            else sanitized.substring(0, idx + 1) +
                sanitized.substring(idx + 1).replace(".", "")
        }
        _state.update { it.copy(amount = onlyOneDot.take(12)) }
    }

    fun onPurposeChange(value: String) {
        _state.update { it.copy(purpose = value) }
    }

    fun onRemarksChange(value: String) {
        _state.update { it.copy(remarks = value.take(80)) }
    }

    fun onSendClick() {
        val current = _state.value
        if (!current.canSend) return
        _state.update { it.copy(isSending = true, error = null) }
        viewModelScope.launch {
            val r = repository.executeTransfer(
                fromAccountId = current.fromAccountId,
                toAccountNumber = current.toAccountNumber,
                amount = current.amountValue,
                purpose = current.purpose,
                remarks = current.remarks.ifBlank { null },
                idempotencyKey = UUID.randomUUID().toString()
            )
            when (r) {
                is Resource.Success -> _state.update {
                    it.copy(isSending = false, success = r.data)
                }
                is Resource.Error -> _state.update {
                    it.copy(isSending = false, error = r.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeSuccess() {
        _state.update { it.copy(success = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
