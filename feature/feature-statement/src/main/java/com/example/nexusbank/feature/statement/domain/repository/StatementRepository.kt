package com.example.nexusbank.feature.statement.domain.repository

import android.net.Uri
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.statement.domain.model.Statement

interface StatementRepository {
    /**
     * Fetches the statement. If [accountId] is null we resolve the user's
     * primary account via /auth/me first.
     */
    suspend fun getStatement(
        accountId: String? = null,
        from: String? = null,
        to: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Resource<Statement>

    /**
     * Downloads the statement file (pdf or csv) and persists it to the device's
     * Downloads folder. Returns a content [Uri] that can be opened or shared.
     */
    suspend fun downloadStatement(
        accountId: String? = null,
        format: String = "pdf",
        from: String? = null,
        to: String? = null
    ): Resource<Uri>
}
