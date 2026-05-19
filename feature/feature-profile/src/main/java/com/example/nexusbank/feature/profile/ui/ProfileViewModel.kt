package com.example.nexusbank.feature.profile.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.ProfileUpdateData
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.profile.domain.usecase.GetUserUseCase
import com.example.nexusbank.feature.profile.domain.usecase.RefreshProfileUseCase
import com.example.nexusbank.feature.profile.domain.usecase.UpdateProfilePictureUseCase
import com.example.nexusbank.feature.profile.domain.usecase.UpdateProfileUseCase
import com.example.nexusbank.feature.profile.util.encodeImageAsBase64DataUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isUploadingPicture: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val updateCompleted: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val app: Application,
    private val getUserUseCase: GetUserUseCase,
    private val refreshProfileUseCase: RefreshProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updateProfilePictureUseCase: UpdateProfilePictureUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeCache()
        refresh()
    }

    private fun observeCache() {
        viewModelScope.launch {
            getUserUseCase().collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(user = result.data, error = null) }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = refreshProfileUseCase()) {
                is Resource.Success ->
                    _uiState.update { it.copy(isLoading = false, user = result.data, error = null) }
                is Resource.Error ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateProfile(data: ProfileUpdateData) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdating = true,
                    error = null,
                    successMessage = null,
                    updateCompleted = false
                )
            }
            when (val result = updateProfileUseCase(data)) {
                is Resource.Success ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            user = result.data.user,
                            successMessage = result.data.message ?: "Profile updated successfully",
                            updateCompleted = true,
                            error = null
                        )
                    }
                is Resource.Error ->
                    _uiState.update {
                        it.copy(
                            isUpdating = false,
                            error = result.message,
                            updateCompleted = false
                        )
                    }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeUpdateCompleted() {
        _uiState.update { it.copy(updateCompleted = false, successMessage = null) }
    }

    /**
     * Reads the picked image, scales it to a 512px JPEG, Base64-encodes it
     * as a `data:image/jpeg;base64,...` URI and PATCHes it to the backend.
     */
    fun onProfilePicturePicked(uri: Uri) {
        Log.d("ProfilePic", "VM: encoding picked uri=$uri")
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPicture = true, error = null) }
            val dataUri = encodeImageAsBase64DataUri(app, uri)
            if (dataUri == null) {
                Log.w("ProfilePic", "VM: encoding returned null")
                _uiState.update {
                    it.copy(
                        isUploadingPicture = false,
                        error = "Could not read selected image"
                    )
                }
                return@launch
            }
            Log.d("ProfilePic", "VM: encoded base64 length=${dataUri.length}, calling PATCH /profile/picture")
            when (val result = updateProfilePictureUseCase(dataUri)) {
                is Resource.Success -> {
                    Log.d("ProfilePic", "VM: PATCH success")
                    _uiState.update {
                        it.copy(isUploadingPicture = false, user = result.data, error = null)
                    }
                }
                is Resource.Error -> {
                    Log.e("ProfilePic", "VM: PATCH error code=${result.code} msg=${result.message}")
                    _uiState.update {
                        it.copy(isUploadingPicture = false, error = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
