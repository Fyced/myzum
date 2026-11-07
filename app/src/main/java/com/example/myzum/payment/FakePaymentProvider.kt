package com.example.myzum.payment

import kotlinx.coroutines.delay
import java.util.UUID

class FakePaymentProvider : PaymentProvider {
    override suspend fun processPayment(amount: Double, currency: String): PaymentResult {
        // Simulate network delay
        delay(2000)
        
        // Always return success for the emulator
        return PaymentResult(
            success = true,
            transactionId = UUID.randomUUID().toString()
        )
    }

    override suspend fun refund(paymentId: String, amount: Double): RefundResult {
        delay(1500)
        return RefundResult.Success(UUID.randomUUID().toString())
    }
}
