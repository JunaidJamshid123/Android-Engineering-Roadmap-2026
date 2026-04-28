package com.example.nexusbank.core.domain.model

data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val description: String,
    val category: String?,
    val referenceId: String,
    val status: TransactionStatus,
    val recipientName: String?,
    val recipientAccount: String?,
    val mode: TransferMode?,
    val timestamp: Long
)

enum class TransactionType { CREDIT, DEBIT }

enum class TransactionStatus { SUCCESS, PENDING, FAILED }

enum class TransferMode { UPI, NEFT, IMPS, RTGS, INTERNAL }
