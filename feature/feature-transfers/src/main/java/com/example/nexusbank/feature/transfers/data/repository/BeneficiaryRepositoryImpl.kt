package com.example.nexusbank.feature.transfers.data.repository

import com.example.nexusbank.core.database.dao.BeneficiaryDao
import com.example.nexusbank.core.domain.model.Beneficiary
import com.example.nexusbank.core.domain.util.Resource
import com.example.nexusbank.feature.transfers.data.mapper.toDomain
import com.example.nexusbank.feature.transfers.domain.repository.BeneficiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BeneficiaryRepositoryImpl @Inject constructor(
    private val beneficiaryDao: BeneficiaryDao
) : BeneficiaryRepository {

    override fun getBeneficiaries(userId: String): Flow<Resource<List<Beneficiary>>> {
        return beneficiaryDao.getBeneficiaries(userId).map { entities ->
            Resource.Success(entities.map { it.toDomain() })
        }
    }

    override fun getBeneficiaryById(id: String): Flow<Resource<Beneficiary>> {
        return beneficiaryDao.getBeneficiaryById(id).map { entity ->
            if (entity != null) Resource.Success(entity.toDomain())
            else Resource.Error("Beneficiary not found")
        }
    }

    override suspend fun deleteBeneficiary(id: String): Resource<Unit> {
        beneficiaryDao.deleteBeneficiary(id)
        return Resource.Success(Unit)
    }
}
