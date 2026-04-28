package com.example.nexusbank.core.domain.model

data class Loan(
    val id: String,
    val userId: String,
    val type: LoanType,
    val principalAmount: Double,
    val outstandingAmount: Double,
    val interestRate: Double,
    val emiAmount: Double,
    val tenureMonths: Int,
    val startDate: Long,
    val endDate: Long,
    val nextEmiDate: Long?,
    val status: LoanStatus
)

enum class LoanType { PERSONAL, HOME, AUTO }

enum class LoanStatus { ACTIVE, CLOSED, DEFAULT }
