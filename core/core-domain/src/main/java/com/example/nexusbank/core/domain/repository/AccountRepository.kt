package com.example.nexusbank.core.domain.repository

import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccounts(): Flow<Resource<List<Account>>>
    fun getAccountById(accountId: String): Flow<Resource<Account>>
}
