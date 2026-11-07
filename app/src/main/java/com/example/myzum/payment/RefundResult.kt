package com.example.myzum.payment

sealed class RefundResult {
    data class Success(val refundId: String) : RefundResult()
    object Pending : RefundResult()
    data class Failed(val message: String) : RefundResult()
}
