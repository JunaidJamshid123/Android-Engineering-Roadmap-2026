package com.example.nexusbank.feature.statement.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.statement.domain.model.Statement
import com.example.nexusbank.feature.statement.domain.usecase.DownloadStatementUseCase
import com.example.nexusbank.feature.statement.domain.usecase.GetStatementUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatementUiState(
    val isLoading: Boolean = false,
    val statement: Statement? = null,
    val error: String? = null,
    val from: String? = null,
    val to: String? = null,
    val isDownloading: Boolean = false
)

sealed interface StatementEvent {
    data class FileDownloaded(val uri: Uri, val mimeType: String) : StatementEvent
    data class ShowMessage(val message: String) : StatementEvent
}

@HiltViewModel
class StatementViewModel @Inject constructor(
    private val getStatementUseCase: GetStatementUseCase,
    private val downloadStatementUseCase: DownloadStatementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(StatementUiState())
    val state: StateFlow<StatementUiState> = _state.asStateFlow()

    private val _events = Channel<StatementEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun load(from: String? = null, to: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, from = from, to = to) }
            when (val res = getStatementUseCase(from = from, to = to)) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, statement = res.data, error = null)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun refresh() = load(_state.value.from, _state.value.to)

    fun clearError() = _state.update { it.copy(error = null) }

    fun downloadStatement(format: String = "pdf") {
        if (_state.value.isDownloading) return
        viewModelScope.launch {
            _state.update { it.copy(isDownloading = true) }
            val s = _state.value
            val accountId = s.statement?.account?.id
            when (val res = downloadStatementUseCase(
                accountId = accountId,
                format = format,
                from = s.from,
                to = s.to
            )) {
                is Resource.Success -> {
                    _state.update { it.copy(isDownloading = false) }
                    val mime = if (format.equals("csv", true)) "text/csv" else "application/pdf"
                    _events.send(StatementEvent.FileDownloaded(res.data, mime))
                }
                is Resource.Error -> {
                    _state.update { it.copy(isDownloading = false) }
                    _events.send(StatementEvent.ShowMessage(res.message ?: "Download failed"))
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
