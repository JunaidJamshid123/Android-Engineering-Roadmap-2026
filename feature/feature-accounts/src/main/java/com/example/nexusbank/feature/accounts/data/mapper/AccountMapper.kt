package com.example.nexusbank.feature.accounts.data.mapper

import com.example.nexusbank.core.database.entity.AccountEntity
import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.model.AccountType
import com.example.nexusbank.core.network.model.AccountDto

fun AccountDto.toEntity(lastSynced: Long = System.currentTimeMillis()): AccountEntity = AccountEntity(
    id = id, userId = userId, accountNumber = accountNumber,
    type = type, balance = balance, currency = currency,
    branchName = branchName, ifscCode = ifscCode, isActive = isActive,
    lastSynced = lastSynced
)

fun AccountEntity.toDomain(): Account = Account(
    id = id, userId = userId, accountNumber = accountNumber,
    type = try { AccountType.valueOf(type) } catch (_: Exception) { AccountType.SAVINGS },
    balance = balance, currency = currency,
    branchName = branchName, ifscCode = ifscCode, isActive = isActive
)

fun AccountDto.toDomain(): Account = Account(
    id = id, userId = userId, accountNumber = accountNumber,
    type = try { AccountType.valueOf(type) } catch (_: Exception) { AccountType.SAVINGS },
    balance = balance, currency = currency,
    branchName = branchName, ifscCode = ifscCode, isActive = isActive
)
