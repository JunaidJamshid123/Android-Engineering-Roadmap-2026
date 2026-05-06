package com.example.nexusbank.feature.transfers.domain

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.model.BankAccountDto
import com.example.nexusbank.core.network.model.ResolveRecipientData
import com.example.nexusbank.core.network.model.TransferHistoryData
import com.example.nexusbank.core.network.model.TransferResponseData

interface TransferRepository {

    suspend fun getMyAccounts(): Resource<List<BankAccountDto>>

    suspend fun resolveRecipient(accountNumber: String): Resource<ResolveRecipientData>

    suspend fun executeTransfer(
        fromAccountId: String,
        toAccountNumber: String,
        amount: Double,
        purpose: String,
        remarks: String?,
        idempotencyKey: String
    ): Resource<TransferResponseData>

    suspend fun getTransferHistory(limit: Int = 20, offset: Int = 0): Resource<TransferHistoryData>
}
