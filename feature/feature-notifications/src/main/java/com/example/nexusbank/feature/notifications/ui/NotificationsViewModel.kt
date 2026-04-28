package com.example.nexusbank.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.model.Notification
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.security.EncryptedPrefs
import com.example.nexusbank.feature.notifications.domain.repository.NotificationRepository
import com.example.nexusbank.feature.notifications.domain.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val notificationRepository: NotificationRepository,
    private val encryptedPrefs: EncryptedPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() {
        val userId = encryptedPrefs.userId ?: return
        viewModelScope.launch {
            getNotificationsUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update { it.copy(isLoading = false, notifications = result.data) }
                    is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch { notificationRepository.markAsRead(notificationId) }
    }

    fun markAllAsRead() {
        val userId = encryptedPrefs.userId ?: return
        viewModelScope.launch { notificationRepository.markAllAsRead(userId) }
    }
}
