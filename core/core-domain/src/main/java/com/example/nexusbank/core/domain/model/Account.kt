package com.example.nexusbank.core.domain.model

data class Account(
    val id: String,
    val userId: String,
    val accountNumber: String,
    val type: AccountType,
    val balance: Double,
    val currency: String,
    val branchName: String,
    val ifscCode: String,
    val isActive: Boolean
)

enum class AccountType {
    SAVINGS, CURRENT, FIXED_DEPOSIT, RECURRING_DEPOSIT
}
