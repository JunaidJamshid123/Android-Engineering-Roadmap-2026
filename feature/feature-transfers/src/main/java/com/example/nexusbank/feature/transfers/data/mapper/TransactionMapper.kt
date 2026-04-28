package com.example.nexusbank.feature.transfers.data.mapper

import com.example.nexusbank.core.database.entity.BeneficiaryEntity
import com.example.nexusbank.core.database.entity.TransactionEntity
import com.example.nexusbank.core.domain.model.*
import com.example.nexusbank.core.network.model.TransactionDto

fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = id, accountId = accountId, type = type, amount = amount,
    currency = currency, description = description, category = category,
    referenceId = referenceId, status = status, recipientName = recipientName,
    recipientAccount = recipientAccount, mode = mode, timestamp = timestamp
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id, accountId = accountId,
    type = try { TransactionType.valueOf(type) } catch (_: Exception) { TransactionType.DEBIT },
    amount = amount, currency = currency, description = description,
    category = category, referenceId = referenceId,
    status = try { TransactionStatus.valueOf(status) } catch (_: Exception) { TransactionStatus.SUCCESS },
    recipientName = recipientName, recipientAccount = recipientAccount,
    mode = mode?.let { try { TransferMode.valueOf(it) } catch (_: Exception) { null } },
    timestamp = timestamp
)

fun BeneficiaryEntity.toDomain(): Beneficiary = Beneficiary(
    id = id, userId = userId, name = name, accountNumber = accountNumber,
    ifscCode = ifscCode, bankName = bankName, nickname = nickname,
    transferLimit = transferLimit, isVerified = isVerified, createdAt = createdAt
)
