package com.example.myzum.model

enum class FundingSourceType {
    CARD,
    ACCOUNT
}

data class FundingSource(
    val id: String,
    val type: FundingSourceType,
    val label: String, // e.g. "My Visa", "Business Account"
    val last4: String, // last 4 digits of card or IBAN
    val brand: String? = null, // e.g. "VISA", "MASTERCARD" (for CARD)
    val bankName: String? = null, // e.g. "Santander" (for ACCOUNT)
    val ownerId: String
)
