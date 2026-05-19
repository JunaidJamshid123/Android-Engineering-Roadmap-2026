package com.example.nexusbank.feature.profile.data.mapper

import com.example.nexusbank.core.database.entity.UserEntity
import com.example.nexusbank.core.domain.model.KycStatus
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.network.model.UserDto

private fun parseKyc(raw: String?): KycStatus = try {
    KycStatus.valueOf(raw ?: KycStatus.NOT_STARTED.name)
} catch (_: Exception) {
    KycStatus.NOT_STARTED
}

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    userId = userId,
    fullName = fullName,
    phone = phone,
    email = email,
    dateOfBirth = dateOfBirth,
    gender = gender,
    kycStatus = kycStatus,
    fatherName = fatherName,
    cnic = cnic,
    maritalStatus = maritalStatus,
    nationality = nationality,
    occupation = occupation,
    monthlyIncome = monthlyIncome,
    addressLine = addressLine,
    city = city,
    country = country,
    emergencyContactName = emergencyContactName,
    emergencyContactPhone = emergencyContactPhone,
    profilePictureUrl = profilePictureUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserEntity.toDomain(): User = User(
    id = id,
    userId = userId,
    fullName = fullName,
    phone = phone,
    email = email,
    dateOfBirth = dateOfBirth,
    gender = gender,
    kycStatus = parseKyc(kycStatus),
    fatherName = fatherName,
    cnic = cnic,
    maritalStatus = maritalStatus,
    nationality = nationality,
    occupation = occupation,
    monthlyIncome = monthlyIncome,
    addressLine = addressLine,
    city = city,
    country = country,
    emergencyContactName = emergencyContactName,
    emergencyContactPhone = emergencyContactPhone,
    profilePictureUrl = profilePictureUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserDto.toDomain(): User = User(
    id = id,
    userId = userId,
    fullName = fullName,
    phone = phone,
    email = email,
    dateOfBirth = dateOfBirth,
    gender = gender,
    kycStatus = parseKyc(kycStatus),
    fatherName = fatherName,
    cnic = cnic,
    maritalStatus = maritalStatus,
    nationality = nationality,
    occupation = occupation,
    monthlyIncome = monthlyIncome,
    addressLine = addressLine,
    city = city,
    country = country,
    emergencyContactName = emergencyContactName,
    emergencyContactPhone = emergencyContactPhone,
    profilePictureUrl = profilePictureUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)
