package com.example.myzum.repository

import com.example.myzum.model.Payment
import com.example.myzum.repository.local.PaymentDao
import com.example.myzum.repository.local.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentRepository(private val paymentDao: PaymentDao) {
    
    fun getPayments(userId: String): Flow<List<Payment>> = paymentDao.getAllPayments(userId).map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun savePayment(payment: Payment) {
        paymentDao.insertPayment(payment.toEntity())
    }

    suspend fun getPaymentById(id: String, userId: String): Payment? {
        return paymentDao.getPaymentById(id, userId)?.toDomain()
    }
}

private fun PaymentEntity.toDomain(): Payment {
    return Payment(
        id = id,
        amount = amount,
        currency = currency,
        date = date,
        status = status,
        fundingSourceId = fundingSourceId,
        ownerId = ownerId,
        refundedAmount = refundedAmount,
        parentPaymentId = parentPaymentId
    )
}

private fun Payment.toEntity(): PaymentEntity {
    return PaymentEntity(
        id = id,
        amount = amount,
        currency = currency,
        date = date,
        status = status,
        fundingSourceId = fundingSourceId,
        ownerId = ownerId,
        refundedAmount = refundedAmount,
        parentPaymentId = parentPaymentId
    )
}
