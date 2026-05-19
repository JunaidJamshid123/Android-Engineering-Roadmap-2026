package com.example.nexusbank.feature.transfers.data

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.TransferApiService
import com.example.nexusbank.core.network.api.UserApiService
import com.example.nexusbank.core.network.model.BankAccountDto
import com.example.nexusbank.core.network.model.ResolveRecipientData
import com.example.nexusbank.core.network.model.ResolveRecipientRequest
import com.example.nexusbank.core.network.model.TransferRequest
import com.example.nexusbank.core.network.model.TransferResponseData
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.feature.transfers.domain.TransferRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val api: TransferApiService,
    private val userApi: UserApiService
) : TransferRepository {

    override suspend fun getMyAccounts(): Resource<List<BankAccountDto>> {
        return when (val result = safeApiCall { userApi.getMe() }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.success && body.data != null) {
                    Resource.Success(body.data!!.bankAccounts)
                } else {
                    Resource.Error(body.message ?: "Could not load accounts")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun resolveRecipient(accountNumber: String): Resource<ResolveRecipientData> {
        return when (val result = safeApiCall { api.resolveRecipient(ResolveRecipientRequest(accountNumber)) }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.success && body.data != null) {
                    Resource.Success(body.data!!)
                } else {
                    Resource.Error(body.message ?: "Could not resolve recipient")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }

    override suspend fun executeTransfer(
        fromAccountId: String,
        toAccountNumber: String,
        amount: Double,
        purpose: String,
        remarks: String?,
        idempotencyKey: String
    ): Resource<TransferResponseData> {
        val req = TransferRequest(
            fromAccountId = fromAccountId,
            toAccountNumber = toAccountNumber,
            amount = amount,
            purpose = purpose,
            remarks = remarks,
            idempotencyKey = idempotencyKey
        )
        return when (val result = safeApiCall { api.executeTransfer(req) }) {
            is NetworkResult.Success -> {
                val body = result.data
                if (body.success && body.data != null) {
                    Resource.Success(body.data!!)
                } else {
                    Resource.Error(body.message ?: "Transfer failed")
                }
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }
}
