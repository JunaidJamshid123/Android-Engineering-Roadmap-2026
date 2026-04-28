package com.example.nexusbank.feature.loans.domain.usecase

import com.example.nexusbank.core.domain.model.Loan
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.loans.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoansUseCase @Inject constructor(
    private val loanRepository: LoanRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<Loan>>> = loanRepository.getLoans(userId)
}
