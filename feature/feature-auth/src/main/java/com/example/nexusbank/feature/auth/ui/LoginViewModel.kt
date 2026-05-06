package com.example.nexusbank.feature.auth.ui
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class LoginUiState(
    val phone: String = "",
    val password: String = "",
    val mpin: String = "",
    val isPasswordVisible: Boolean = false,
    val isMpinVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '+' }
        if (filtered.length <= 13) {
            _uiState.update { it.copy(phone = filtered, error = null) }
        }
    }

    fun onPasswordChange(value: String) {
        val sanitized = value.replace(Regex("[\\t\\n\\r]"), "")
        if (sanitized.length <= 64) {
            _uiState.update { it.copy(password = sanitized, error = null) }
        }
    }

    fun onMpinChange(value: String) {
        val filtered = value.filter { it.isDigit() }
        if (filtered.length <= 4) {
            _uiState.update { it.copy(mpin = filtered, error = null) }
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onToggleMpinVisibility() {
        _uiState.update { it.copy(isMpinVisible = !it.isMpinVisible) }
    }

    fun onLoginClick() {
        val state = _uiState.value
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(error = "Phone number is required") }
            return
        }
        if (!state.phone.matches(Regex("^\\+?[0-9]{10,13}$"))) {
            _uiState.update { it.copy(error = "Invalid phone number format") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(error = "Password is required") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }
        if (state.mpin.isBlank() || state.mpin.length != 4) {
            _uiState.update { it.copy(error = "4-digit MPIN is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.login(state.phone, state.password, state.mpin)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> { /* handled by isLoading flag */ }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
