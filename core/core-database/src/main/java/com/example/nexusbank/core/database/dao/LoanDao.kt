package com.example.nexusbank.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nexusbank.core.database.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Query("SELECT * FROM loans WHERE userId = :userId")
    fun getLoansByUserId(userId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    fun getLoanById(loanId: String): Flow<LoanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Query("DELETE FROM loans")
    suspend fun deleteAll()
}
