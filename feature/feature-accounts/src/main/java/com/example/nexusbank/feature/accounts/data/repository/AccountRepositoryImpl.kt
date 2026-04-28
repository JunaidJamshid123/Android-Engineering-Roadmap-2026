package com.example.nexusbank.feature.accounts.data.repository

import com.example.nexusbank.core.database.dao.AccountDao
import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.repository.AccountRepository
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.core.network.api.NexusBankApiService
import com.example.nexusbank.core.network.util.NetworkResult
import com.example.nexusbank.core.network.util.safeApiCall
import com.example.nexusbank.feature.accounts.data.mapper.toDomain
import com.example.nexusbank.feature.accounts.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val apiService: NexusBankApiService,
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAccounts(): Flow<Resource<List<Account>>> {
        return accountDao.getAllAccounts().map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getAccountById(accountId: String): Flow<Resource<Account>> {
        return accountDao.getAccountById(accountId).map { entity ->
            if (entity != null) {
                Resource.Success(entity.toDomain())
            } else {
                Resource.Error("Account not found")
            }
        }
    }

    suspend fun refreshAccounts(): Resource<List<Account>> {
        return when (val result = safeApiCall { apiService.getAccounts() }) {
            is NetworkResult.Success -> {
                val entities = result.data.accounts.map { it.toEntity() }
                accountDao.insertAccounts(entities)
                Resource.Success(result.data.accounts.map { it.toDomain() })
            }
            is NetworkResult.Error -> Resource.Error(result.message, result.code)
        }
    }
}
