package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String,
    val accountId: String,
    val type: String,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String? = null,
    val referenceId: String,
    val status: String,
    val recipientName: String? = null,
    val recipientAccount: String? = null,
    val mode: String? = null,
    val timestamp: Long
)

@Serializable
data class TransactionsResponse(
    val transactions: List<TransactionDto>,
    val pagination: PaginationDto? = null
)

@Serializable
data class PaginationDto(
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalItems: Int
)

@Serializable
data class TransactionResponse(
    val txn: TransactionDto
)
