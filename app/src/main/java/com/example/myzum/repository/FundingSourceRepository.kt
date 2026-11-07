package com.example.myzum.repository

import com.example.myzum.model.FundingSource
import com.example.myzum.repository.local.FundingSourceDao
import com.example.myzum.repository.local.FundingSourceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FundingSourceRepository(private val dao: FundingSourceDao) {

    fun getFundingSources(userId: String): Flow<List<FundingSource>> = dao.getAllFundingSources(userId).map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun addFundingSource(source: FundingSource) {
        dao.insertFundingSource(source.toEntity())
    }

    suspend fun removeFundingSource(id: String, userId: String) {
        dao.deleteFundingSource(id, userId)
    }
}

private fun FundingSourceEntity.toDomain() = FundingSource(
    id = id,
    type = type,
    label = label,
    last4 = last4,
    brand = brand,
    bankName = bankName,
    ownerId = ownerId
)

private fun FundingSource.toEntity() = FundingSourceEntity(
    id = id,
    type = type,
    label = label,
    last4 = last4,
    brand = brand,
    bankName = bankName,
    ownerId = ownerId
)
