package com.example.myzum.model.payment

import java.util.Date

data class Refund(
    val id: String,
    val paymentId: String,
    val accountId: String,
    val amount: Double,
    val date: Date,
    val status: PaymentStatus,
    val reason: String?,
    val providerRefundId: String? = null
)
