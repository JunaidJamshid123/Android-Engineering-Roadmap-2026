package com.example.nexusbank.feature.profile.data.mapper

import com.example.nexusbank.core.database.entity.UserEntity
import com.example.nexusbank.core.domain.model.KycStatus
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.network.model.UserDto

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    kycStatus = kycStatus,
    createdAt = createdAt
)

fun UserEntity.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    kycStatus = try { KycStatus.valueOf(kycStatus) } catch (_: Exception) { KycStatus.NOT_STARTED },
    createdAt = createdAt
)

fun UserDto.toDomain(): User = User(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    kycStatus = try { KycStatus.valueOf(kycStatus) } catch (_: Exception) { KycStatus.NOT_STARTED },
    createdAt = createdAt
)
