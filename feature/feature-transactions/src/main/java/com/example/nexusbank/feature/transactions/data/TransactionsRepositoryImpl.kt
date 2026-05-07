package com.example.nexusbank.feature.transactions.data

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.TransferApiService
import com.example.nexusbank.core.network.model.TransferHistoryData
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.feature.transactions.domain.TransactionsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionsRepositoryImpl @Inject constructor(
    private val api: TransferApiService
) : TransactionsRepository {

    override suspend fun getHistory(limit: Int, offset: Int): Resource<TransferHistoryData> {
        return when (val result = safeApiCall { api.getTransferHistory(limit, offset) }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.success && body.data != null) {
                    Resource.Success(body.data!!)
                } else {
                    Resource.Error(body.message ?: "Could not load transactions")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }
}
