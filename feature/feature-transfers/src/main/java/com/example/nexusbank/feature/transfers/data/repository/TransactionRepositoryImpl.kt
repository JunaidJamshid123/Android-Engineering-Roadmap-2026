package com.example.nexusbank.feature.transfers.data.repository

import com.example.nexusbank.core.database.dao.TransactionDao
import com.example.nexusbank.core.domain.model.Transaction
import com.example.nexusbank.core.domain.repository.TransactionRepository
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.transfers.data.mapper.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getRecentTransactions(accountId: String, limit: Int): Flow<Resource<List<Transaction>>> {
        return transactionDao.getRecentTransactions(accountId, limit).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getTransactionById(txnId: String): Flow<Resource<Transaction>> {
        return transactionDao.getTransactionById(txnId).map { entity ->
            if (entity != null) Resource.Success(entity.toDomain())
            else Resource.Error("Transaction not found")
        }
    }
}
