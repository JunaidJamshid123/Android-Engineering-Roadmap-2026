package com.example.nexusbank.feature.accounts.domain.usecase

import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.repository.AccountRepository
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccountByIdUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(accountId: String): Flow<Resource<Account>> =
        accountRepository.getAccountById(accountId)
}
