package com.example.myzum.repository

import com.example.myzum.model.payment.PaymentStatus
import com.example.myzum.model.payment.Refund
import com.example.myzum.payment.PaymentProvider
import com.example.myzum.payment.RefundResult
import com.example.myzum.repository.local.PaymentDao
import com.example.myzum.repository.local.RefundDao
import com.example.myzum.repository.local.RefundEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class RefundRepository(
    private val refundDao: RefundDao,
    private val paymentDao: PaymentDao,
    private val paymentProvider: PaymentProvider
) {
    fun getRefundsForPayment(paymentId: String, userId: String): Flow<List<Refund>> =
        refundDao.getRefundsForPayment(paymentId, userId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun processRefund(paymentId: String, userId: String, amount: Double, reason: String?): RefundResult {
        val payment = paymentDao.getPaymentById(paymentId, userId) ?: return RefundResult.Failed("Payment not found")
        
        if (payment.status != "APPROVED" && payment.status != "PARTIALLY_REFUNDED") {
            return RefundResult.Failed("Payment is not eligible for refund")
        }

        val availableToRefund = payment.amount - payment.refundedAmount
        if (amount > availableToRefund) {
            return RefundResult.Failed("Amount exceeds available balance to refund")
        }

        val result = paymentProvider.refund(payment.transactionReference ?: paymentId, amount)

        if (result is RefundResult.Success) {
            val refundEntity = RefundEntity(
                id = UUID.randomUUID().toString(),
                paymentId = paymentId,
                accountId = userId,
                amount = amount,
                date = Date(),
                status = "APPROVED",
                reason = reason,
                providerRefundId = result.refundId
            )
            refundDao.insertRefund(refundEntity)

            // Update original payment
            val newRefundedAmount = payment.refundedAmount + amount
            val newStatus = if (newRefundedAmount >= payment.amount) "REFUNDED" else "PARTIALLY_REFUNDED"
            
            paymentDao.insertPayment(payment.copy(
                refundedAmount = newRefundedAmount,
                status = newStatus
            ))

            // ALSO: Create a new Payment entry representing the refund movement for history visibility
            val refundMovement = com.example.myzum.repository.local.PaymentEntity(
                id = UUID.randomUUID().toString(),
                amount = -amount, // Negative amount for refund
                currency = payment.currency,
                date = Date(),
                status = "APPROVED", // The movement itself is approved
                paymentMethod = payment.paymentMethod,
                transactionReference = result.refundId,
                fundingSourceId = payment.fundingSourceId,
                ownerId = userId,
                parentPaymentId = paymentId
            )
            paymentDao.insertPayment(refundMovement)
        }

        return result
    }
}

private fun RefundEntity.toDomain() = Refund(
    id = id,
    paymentId = paymentId,
    accountId = accountId,
    amount = amount,
    date = date,
    status = PaymentStatus.valueOf(status),
    reason = reason,
    providerRefundId = providerRefundId
)
