package com.example.myzum.model

data class Merchant(
    val id: String,
    val name: String,
    val address: String,
    val providerId: String,
    val currency: String,
    val configuration: Map<String, String> = emptyMap()
)
