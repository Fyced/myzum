package com.example.myzum.repository.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val amount: Double,
    val currency: String,
    val date: Date,
    val status: String,
    val paymentMethod: String = "NFC",
    val transactionReference: String? = null,
    val fundingSourceId: String? = null,
    val ownerId: String,
    val refundedAmount: Double = 0.0,
    val parentPaymentId: String? = null
)
