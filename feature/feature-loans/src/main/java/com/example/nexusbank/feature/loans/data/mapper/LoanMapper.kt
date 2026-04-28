package com.example.nexusbank.feature.loans.data.mapper

import com.example.nexusbank.core.database.entity.LoanEntity
import com.example.nexusbank.core.domain.model.Loan
import com.example.nexusbank.core.domain.model.LoanStatus
import com.example.nexusbank.core.domain.model.LoanType

fun LoanEntity.toDomain(): Loan = Loan(
    id = id, userId = userId,
    type = try { LoanType.valueOf(type) } catch (_: Exception) { LoanType.PERSONAL },
    principalAmount = principalAmount, outstandingAmount = outstandingAmount,
    interestRate = interestRate, emiAmount = emiAmount, tenureMonths = tenureMonths,
    startDate = startDate, endDate = endDate, nextEmiDate = nextEmiDate,
    status = try { LoanStatus.valueOf(status) } catch (_: Exception) { LoanStatus.ACTIVE }
)
