package com.example.myzum.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE ownerId = :userId ORDER BY date DESC")
    fun getAllPayments(userId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE id = :id AND ownerId = :userId")
    suspend fun getPaymentById(id: String, userId: String): PaymentEntity?
}
