package com.example.nexusbank.feature.statement.domain.usecase

import android.net.Uri
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.statement.domain.repository.StatementRepository
import javax.inject.Inject

class DownloadStatementUseCase @Inject constructor(
    private val repository: StatementRepository
) {
    suspend operator fun invoke(
        accountId: String? = null,
        format: String = "pdf",
        from: String? = null,
        to: String? = null
    ): Resource<Uri> = repository.downloadStatement(accountId, format, from, to)
}
