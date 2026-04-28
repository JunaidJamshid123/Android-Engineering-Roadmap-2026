package com.example.nexusbank.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.AccountRepository
import com.example.nexusbank.core.domain.repository.UserRepository
import com.example.nexusbank.core.domain.util.Resource
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
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            launch {
                userRepository.getUser().collect { result ->
                    when (result) {
                        is Resource.Success -> _uiState.update { it.copy(user = result.data) }
                        is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                        is Resource.Loading -> { }
                    }
                }
            }
            launch {
                accountRepository.getAccounts().collect { result ->
                    when (result) {
                        is Resource.Success -> _uiState.update { it.copy(isLoading = false, accounts = result.data) }
                        is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                        is Resource.Loading -> { }
                    }
                }
            }
        }
    }
}
