package com.example.myzum.repository

import com.example.myzum.model.*
import com.example.myzum.repository.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    fun getLoggedInUser(): Flow<User?> = userDao.getLoggedInUser().map { it?.toDomain() }

    fun getUserById(id: String): Flow<User?> = userDao.getUserById(id).map { it?.toDomain() }

    suspend fun findUserById(id: String): User? = userDao.findUserById(id)?.toDomain()

    suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }

    suspend fun deleteUser(id: String) {
        userDao.deleteUser(id)
    }
}

private fun UserEntity.toDomain(): User {
    return User(
        id = id,
        password = password,
        type = type,
        personalInfo = personalInfo.toDomain(),
        businessInfo = businessInfo?.toDomain(),
        bankInfo = bankInfo.toDomain(),
        settings = settings.toDomain(),
        isVerified = isVerified,
        isLoggedIn = isLoggedIn
    )
}

private fun PersonalInfoEntity.toDomain() = PersonalInfo(
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    dni = dni,
    address = address?.toDomain()
)

private fun BusinessInfoEntity.toDomain() = BusinessInfo(
    tradeName = tradeName,
    fiscalName = fiscalName,
    nif = nif,
    businessEmail = businessEmail,
    businessPhone = businessPhone,
    address = address?.toDomain() ?: Address("", "", "Spain", ""),
    category = null
)

private fun AddressEntity.toDomain() = Address(
    street = street,
    city = city,
    country = country,
    zipCode = zipCode
)

private fun BankInfoEntity.toDomain() = BankInfo(
    iban = iban,
    bankName = bankName
)

private fun UserSettingsEntity.toDomain() = UserSettings(
    theme = theme,
    notificationsEnabled = notificationsEnabled,
    biometricEnabled = biometricEnabled,
    terminalName = terminalName
)

private fun User.toEntity() = UserEntity(
    id = id,
    password = password,
    type = type,
    personalInfo = personalInfo.toEntity(),
    businessInfo = businessInfo?.toEntity(),
    bankInfo = bankInfo.toEntity(),
    settings = settings.toEntity(),
    isVerified = isVerified,
    isLoggedIn = isLoggedIn
)

private fun PersonalInfo.toEntity() = PersonalInfoEntity(
    firstName = firstName,
    lastName = lastName,
    email = email,
    phone = phone,
    dni = dni,
    address = address?.toEntity()
)

private fun BusinessInfo.toEntity() = BusinessInfoEntity(
    tradeName = tradeName,
    fiscalName = fiscalName,
    nif = nif,
    businessEmail = businessEmail,
    businessPhone = businessPhone,
    address = address.toEntity()
)

private fun Address.toEntity() = AddressEntity(
    street = street,
    city = city,
    country = country,
    zipCode = zipCode
)

private fun BankInfo.toEntity() = BankInfoEntity(
    iban = iban,
    bankName = bankName
)

private fun UserSettings.toEntity() = UserSettingsEntity(
    theme = theme,
    notificationsEnabled = notificationsEnabled,
    biometricEnabled = biometricEnabled,
    terminalName = terminalName
)
