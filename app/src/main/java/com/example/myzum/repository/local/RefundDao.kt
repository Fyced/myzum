package com.example.myzum.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RefundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefund(refund: RefundEntity)

    @Query("SELECT * FROM refunds WHERE id = :id AND accountId = :userId")
    suspend fun getRefundById(id: String, userId: String): RefundEntity?

    @Query("SELECT * FROM refunds WHERE paymentId = :paymentId AND accountId = :userId")
    fun getRefundsForPayment(paymentId: String, userId: String): Flow<List<RefundEntity>>

    @Query("SELECT * FROM refunds WHERE accountId = :userId ORDER BY date DESC")
    fun getAllRefunds(userId: String): Flow<List<RefundEntity>>
}
