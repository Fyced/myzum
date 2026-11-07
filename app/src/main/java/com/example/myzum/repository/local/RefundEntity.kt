package com.example.myzum.repository.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "refunds")
data class RefundEntity(
    @PrimaryKey val id: String,
    val paymentId: String,
    val accountId: String,
    val amount: Double,
    val date: Date,
    val status: String,
    val reason: String?,
    val providerRefundId: String?
)
