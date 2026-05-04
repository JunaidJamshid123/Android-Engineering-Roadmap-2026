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

data class SignUpUiState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val password: String = "",
    val mpin: String = "",
    val isPasswordVisible: Boolean = false,
    val isMpinVisible: Boolean = false,
    val termsAccepted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpSuccess: Boolean = false
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, error = null) }
    }

    fun onPhoneChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '+' }
        if (filtered.length <= 13) {
            _uiState.update { it.copy(phone = filtered, error = null) }
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onDateOfBirthChange(value: String) {
        _uiState.update { it.copy(dateOfBirth = value, error = null) }
    }

    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
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

    fun onTermsAcceptedChange(value: Boolean) {
        _uiState.update { it.copy(termsAccepted = value) }
    }

    fun onSignUpClick() {
        val state = _uiState.value
        if (state.fullName.isBlank()) {
            _uiState.update { it.copy(error = "Full name is required") }
            return
        }
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(error = "Phone number is required") }
            return
        }
        if (state.email.isBlank()) {
            _uiState.update { it.copy(error = "Email is required") }
            return
        }
        if (state.dateOfBirth.isBlank()) {
            _uiState.update { it.copy(error = "Date of birth is required") }
            return
        }
        if (state.gender.isBlank()) {
            _uiState.update { it.copy(error = "Please select gender") }
            return
        }
        if (state.password.length < 8) {
            _uiState.update { it.copy(error = "Password must be at least 8 characters") }
            return
        }
        if (state.mpin.length != 4) {
            _uiState.update { it.copy(error = "MPIN must be 4 digits") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.register(
                phone = state.phone,
                fullName = state.fullName,
                email = state.email,
                dateOfBirth = state.dateOfBirth,
                gender = state.gender,
                password = state.password,
                mpin = state.mpin
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSignUpSuccess = true) }
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
