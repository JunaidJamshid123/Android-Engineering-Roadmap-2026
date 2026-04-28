package com.example.nexusbank.feature.loans.data.repository

import com.example.nexusbank.core.database.dao.LoanDao
import com.example.nexusbank.core.domain.model.Loan
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.loans.data.mapper.toDomain
import com.example.nexusbank.feature.loans.domain.repository.LoanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepositoryImpl @Inject constructor(
    private val loanDao: LoanDao
) : LoanRepository {

    override fun getLoans(userId: String): Flow<Resource<List<Loan>>> {
        return loanDao.getLoansByUserId(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getLoanById(loanId: String): Flow<Resource<Loan>> {
        return loanDao.getLoanById(loanId).map { entity ->
            if (entity != null) Resource.Success(entity.toDomain())
            else Resource.Error("Loan not found")
        }
    }
}
