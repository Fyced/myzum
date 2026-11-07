package com.example.myzum.repository.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myzum.model.FundingSourceType

@Entity(tableName = "funding_sources")
data class FundingSourceEntity(
    @PrimaryKey val id: String,
    val type: FundingSourceType,
    val label: String,
    val last4: String,
    val brand: String?,
    val bankName: String?,
    val ownerId: String
)
