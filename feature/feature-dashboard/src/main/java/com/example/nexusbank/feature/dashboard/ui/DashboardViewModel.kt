package com.example.nexusbank.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import com.example.nexusbank.feature.dashboard.mapper.toDomainAccount
import com.example.nexusbank.feature.dashboard.mapper.toDomainUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val user: User? = null,
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.getMe()) {
                is Resource.Success -> {
                    val meData = result.data
                    if (meData != null) {
                        val user = meData.toDomainUser()
                        val accounts = meData.bankAccounts.map { it.toDomainAccount(meData.id) }
                        _uiState.update {
                            it.copy(user = user, accounts = accounts, isLoading = false)
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No data received") }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> { /* handled by isLoading flag */ }
            }
        }
    }

    fun retry() {
        fetchUserProfile()
    }
}
