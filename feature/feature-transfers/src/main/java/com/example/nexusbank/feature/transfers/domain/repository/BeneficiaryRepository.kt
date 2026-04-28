package com.example.nexusbank.feature.transfers.domain.repository

import com.example.nexusbank.core.domain.model.Beneficiary
import com.example.nexusbank.core.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface BeneficiaryRepository {
    fun getBeneficiaries(userId: String): Flow<Resource<List<Beneficiary>>>
    fun getBeneficiaryById(id: String): Flow<Resource<Beneficiary>>
    suspend fun deleteBeneficiary(id: String): Resource<Unit>
}
