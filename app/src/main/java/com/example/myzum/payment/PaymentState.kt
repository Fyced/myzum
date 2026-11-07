package com.example.myzum.payment

sealed class PaymentState {
    object Idle : PaymentState()
    object Processing : PaymentState()
    object Approved : PaymentState()
    object Rejected : PaymentState()
}
