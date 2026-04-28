package com.example.nexusbank.core.domain.repository

import com.example.nexusbank.core.domain.model.Transaction
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getRecentTransactions(accountId: String, limit: Int = 5): Flow<Resource<List<Transaction>>>
    fun getTransactionById(txnId: String): Flow<Resource<Transaction>>
}
