package com.example.myzum.model

import java.util.Date

data class Payment(
    val id: String,
    val amount: Double,
    val currency: String,
    val date: Date,
    val status: String,
    val fundingSourceId: String? = null,
    val ownerId: String,
    val refundedAmount: Double = 0.0,
    val parentPaymentId: String? = null
)
