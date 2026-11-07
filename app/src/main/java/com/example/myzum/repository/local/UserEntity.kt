package com.example.myzum.repository.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myzum.model.AccountType

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String, // DNI/NIF
    val password: String,
    val type: AccountType,
    @Embedded(prefix = "personal_") val personalInfo: PersonalInfoEntity,
    @Embedded(prefix = "business_") val businessInfo: BusinessInfoEntity?,
    @Embedded(prefix = "bank_") val bankInfo: BankInfoEntity,
    @Embedded(prefix = "settings_") val settings: UserSettingsEntity,
    val isVerified: Boolean,
    val isLoggedIn: Boolean
)

data class PersonalInfoEntity(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val dni: String?,
    @Embedded(prefix = "addr_") val address: AddressEntity?
)

data class BusinessInfoEntity(
    val tradeName: String,
    val fiscalName: String?,
    val nif: String?,
    val businessEmail: String?,
    val businessPhone: String?,
    @Embedded(prefix = "biz_addr_") val address: AddressEntity?
)

data class AddressEntity(
    val street: String,
    val city: String,
    val country: String,
    val zipCode: String
)

data class BankInfoEntity(
    val iban: String,
    val bankName: String?
)

data class UserSettingsEntity(
    val theme: String,
    val notificationsEnabled: Boolean,
    val biometricEnabled: Boolean,
    val terminalName: String
)
