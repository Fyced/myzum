package com.example.myzum.repository.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface StatisticsDao {
    @Query("""
        SELECT * FROM payments 
        WHERE ownerId = :userId 
        AND status IN ('APPROVED', 'REFUNDED', 'PARTIALLY_REFUNDED') 
        AND date BETWEEN :start AND :endDate
        ORDER BY date ASC
    """)
    fun getApprovedPaymentsInRange(userId: String, start: Date, endDate: Date): Flow<List<PaymentEntity>>
}

data class PaymentMethodStatRow(
    val method: String,
    val count: Int,
    val totalAmount: Double
)
