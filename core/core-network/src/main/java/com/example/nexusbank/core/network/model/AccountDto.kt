package com.example.nexusbank.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val id: String,
    val userId: String,
    val accountNumber: String,
    val type: String,
    val balance: Double,
    val currency: String,
    val branchName: String,
    val ifscCode: String,
    val isActive: Boolean
)

@Serializable
data class AccountsResponse(
    val accounts: List<AccountDto>
)

@Serializable
data class AccountResponse(
    val account: AccountDto
)
