package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

// ── Resolve Recipient ──

@Serializable
data class ResolveRecipientRequest(
    val accountNumber: String
)

@Serializable
data class ResolveRecipientData(
    val accountId: String,
    val accountNumberMasked: String,
    val holderName: String,
    val accountType: String,
    val currency: String,
    val status: String,
    val isSelf: Boolean
)

// ── Execute Transfer ──

@Serializable
data class TransferRequest(
    val fromAccountId: String,
    val toAccountNumber: String,
    val amount: Double,
    val purpose: String,
    val remarks: String? = null,
    val idempotencyKey: String
)

// ── Transfer History (GET /api/transfer) ──

@Serializable
data class TransferHistoryData(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val items: List<TransferHistoryItem> = emptyList()
)

@Serializable
data class TransferHistoryItem(
    val id: String,
    val referenceNumber: String,
    val transferType: String,
    val direction: String, // "DEBIT" or "CREDIT"
    val accountId: String,
    val counterpartyAccountNumberMasked: String? = null,
    val counterpartyName: String? = null,
    val amount: String,
    val fee: String,
    val netAmount: String,
    val currency: String,
    val runningBalance: String,
    val status: String,
    val purpose: String? = null,
    val remarks: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class TransferResponseData(
    val transferId: String,
    val referenceNumber: String,
    val status: String,
    val transferType: String,
    val amount: String,
    val fee: String,
    val netAmount: String,
    val currency: String,
    val fromAccountId: String,
    val toAccountId: String,
    val toAccountNumberMasked: String,
    val toName: String,
    val runningBalance: String,
    val purpose: String,
    val remarks: String? = null,
    val completedAt: String? = null,
    val createdAt: String? = null
)
