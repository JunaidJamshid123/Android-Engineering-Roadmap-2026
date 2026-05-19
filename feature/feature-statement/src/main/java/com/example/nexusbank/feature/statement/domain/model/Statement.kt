package com.example.nexusbank.feature.statement.domain.model

data class Statement(
    val account: StatementAccount,
    val periodFrom: String,
    val periodTo: String,
    val openingBalance: Double,
    val closingBalance: Double,
    val currentBalance: Double,
    val summary: StatementSummary,
    val pagination: StatementPagination,
    val transactions: List<StatementTxn>
)

data class StatementAccount(
    val id: String,
    val accountNumberMasked: String,
    val accountType: String,
    val currency: String,
    val status: String,
    val holderName: String
)

data class StatementSummary(
    val totalCredits: Double,
    val totalDebits: Double,
    val totalFees: Double,
    val net: Double,
    val count: Int
)

data class StatementPagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

data class StatementTxn(
    val id: String,
    val referenceNumber: String,
    val date: String,
    val direction: TxnDirection,
    val amount: Double,
    val fee: Double,
    val netAmount: Double,
    val currency: String,
    val runningBalance: Double,
    val status: String,
    val counterpartyName: String?,
    val counterpartyAccountMasked: String?,
    val purpose: String?,
    val remarks: String?
)

enum class TxnDirection { CREDIT, DEBIT }
