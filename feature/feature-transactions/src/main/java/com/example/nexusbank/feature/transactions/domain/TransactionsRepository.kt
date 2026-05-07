package com.example.nexusbank.feature.transactions.domain

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.TransferHistoryData

/**
 * Read-only access to a user's transfer / transaction history.
 */
interface TransactionsRepository {
    suspend fun getHistory(limit: Int = 50, offset: Int = 0): Resource<TransferHistoryData>
}
