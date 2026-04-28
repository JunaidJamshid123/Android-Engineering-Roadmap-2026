package com.example.nexusbank.feature.loans.domain.repository

import com.example.nexusbank.core.domain.model.Loan
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun getLoans(userId: String): Flow<Resource<List<Loan>>>
    fun getLoanById(loanId: String): Flow<Resource<Loan>>
}
