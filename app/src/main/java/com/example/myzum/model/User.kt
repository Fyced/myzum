package com.example.myzum.model

enum class AccountType {
    PARTICULAR,
    MERCHANT
}

data class User(
    val id: String, // This will be the DNI/NIF
    val password: String,
    val type: AccountType,
    val personalInfo: PersonalInfo,
    val businessInfo: BusinessInfo? = null,
    val bankInfo: BankInfo,
    val settings: UserSettings = UserSettings(),
    val isVerified: Boolean = false,
    val isLoggedIn: Boolean = false
)

data class PersonalInfo(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val dni: String? = null,
    val address: Address? = null
)

data class BusinessInfo(
    val tradeName: String,
    val fiscalName: String? = null,
    val nif: String? = null,
    val businessEmail: String? = null,
    val businessPhone: String? = null,
    val address: Address,
    val category: String? = null
)

data class Address(
    val street: String,
    val city: String,
    val country: String,
    val zipCode: String
)

data class BankInfo(
    val iban: String,
    val bankName: String? = null
)

data class UserSettings(
    val theme: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val biometricEnabled: Boolean = false,
    val terminalName: String = "My Terminal"
)
