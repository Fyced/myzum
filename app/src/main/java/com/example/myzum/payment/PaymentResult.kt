package com.example.myzum.payment

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val errorMessage: String? = null
)
