package com.example.nexusbank.feature.statement.domain.usecase

import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.statement.domain.model.Statement
import com.example.nexusbank.feature.statement.domain.repository.StatementRepository
import javax.inject.Inject

class GetStatementUseCase @Inject constructor(
    private val repository: StatementRepository
) {
    suspend operator fun invoke(
        accountId: String? = null,
        from: String? = null,
        to: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): Resource<Statement> = repository.getStatement(accountId, from, to, page, limit)
}
