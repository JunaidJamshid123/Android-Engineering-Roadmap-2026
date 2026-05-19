package com.example.nexusbank.feature.statement.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatementData(
    val account: StatementAccountDto,
    val period: StatementPeriodDto,
    val openingBalance: Double,
    val closingBalance: Double,
    val currentBalance: Double,
    val summary: StatementSummaryDto,
    val pagination: StatementPaginationDto,
    val transactions: List<StatementTxnDto>
)

@Serializable
data class StatementAccountDto(
    val id: String,
    val accountNumberMasked: String,
    val accountType: String,
    val currency: String,
    val status: String,
    val holderName: String
)

@Serializable
data class StatementPeriodDto(
    val from: String,
    val to: String
)

@Serializable
data class StatementSummaryDto(
    val totalCredits: Double,
    val totalDebits: Double,
    val totalFees: Double,
    val net: Double,
    val count: Int
)

@Serializable
data class StatementPaginationDto(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int
)

@Serializable
data class StatementTxnDto(
    val id: String,
    val referenceNumber: String,
    val date: String,
    val completedAt: String? = null,
    val direction: String,            // CREDIT | DEBIT
    val transferType: String? = null,
    val amount: Double,
    val fee: Double = 0.0,
    val netAmount: Double,
    val currency: String,
    val runningBalance: Double,
    val status: String,
    val counterpartyAccountNumberMasked: String? = null,
    val counterpartyName: String? = null,
    val purpose: String? = null,
    val remarks: String? = null
)
