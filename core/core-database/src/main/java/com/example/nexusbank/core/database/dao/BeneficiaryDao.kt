package com.example.nexusbank.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nexusbank.core.database.entity.BeneficiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BeneficiaryDao {

    @Query("SELECT * FROM beneficiaries WHERE userId = :userId")
    fun getBeneficiaries(userId: String): Flow<List<BeneficiaryEntity>>

    @Query("SELECT * FROM beneficiaries WHERE id = :id")
    fun getBeneficiaryById(id: String): Flow<BeneficiaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeneficiaries(beneficiaries: List<BeneficiaryEntity>)

    @Query("DELETE FROM beneficiaries WHERE id = :id")
    suspend fun deleteBeneficiary(id: String)

    @Query("DELETE FROM beneficiaries")
    suspend fun deleteAll()
}
