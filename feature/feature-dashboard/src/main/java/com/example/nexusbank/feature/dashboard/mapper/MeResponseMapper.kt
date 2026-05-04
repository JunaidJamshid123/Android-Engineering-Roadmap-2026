package com.example.nexusbank.feature.dashboard.mapper

import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.model.AccountType
import com.example.nexusbank.core.domain.model.KycStatus
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.network.model.BankAccountDto
import com.example.nexusbank.core.network.model.MeResponseData

fun MeResponseData.toDomainUser(): User {
    return User(
        id = id,
        fullName = fullName,
        email = email ?: "",
        phone = phone,
        avatarUrl = avatarUrl,
        kycStatus = when (kycStatus?.uppercase()) {
            "VERIFIED" -> KycStatus.VERIFIED
            "PENDING" -> KycStatus.PENDING
            "REJECTED" -> KycStatus.REJECTED
            else -> KycStatus.NOT_STARTED
        },
        createdAt = 0L
    )
}

fun BankAccountDto.toDomainAccount(userId: String): Account {
    return Account(
        id = id,
        userId = userId,
        accountNumber = accountNumber,
        type = when (accountType.uppercase()) {
            "SAVINGS" -> AccountType.SAVINGS
            "CURRENT" -> AccountType.CURRENT
            "FIXED_DEPOSIT" -> AccountType.FIXED_DEPOSIT
            "RECURRING_DEPOSIT" -> AccountType.RECURRING_DEPOSIT
            else -> AccountType.SAVINGS
        },
        balance = balance.toDoubleOrNull() ?: 0.0,
        currency = currency,
        branchName = "",
        ifscCode = "",
        isActive = status.uppercase() == "ACTIVE"
    )
}
