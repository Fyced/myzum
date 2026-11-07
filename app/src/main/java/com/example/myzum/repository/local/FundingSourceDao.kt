package com.example.myzum.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FundingSourceDao {
    @Query("SELECT * FROM funding_sources WHERE ownerId = :userId")
    fun getAllFundingSources(userId: String): Flow<List<FundingSourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFundingSource(source: FundingSourceEntity)

    @Query("DELETE FROM funding_sources WHERE id = :id AND ownerId = :userId")
    suspend fun deleteFundingSource(id: String, userId: String)
}
