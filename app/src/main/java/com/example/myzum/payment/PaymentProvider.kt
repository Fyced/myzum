package com.example.myzum.payment

interface PaymentProvider {
    suspend fun processPayment(amount: Double, currency: String): PaymentResult
    suspend fun refund(paymentId: String, amount: Double): RefundResult
}
