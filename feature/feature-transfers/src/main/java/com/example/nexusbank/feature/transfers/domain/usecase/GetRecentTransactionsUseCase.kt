package com.example.nexusbank.feature.transfers.domain.usecase

import com.example.nexusbank.core.domain.model.Transaction
import com.example.nexusbank.core.domain.repository.TransactionRepository
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(accountId: String, limit: Int = 10): Flow<Resource<List<Transaction>>> =
        transactionRepository.getRecentTransactions(accountId, limit)
}
