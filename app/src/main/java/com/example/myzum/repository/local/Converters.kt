package com.example.myzum.repository.local

import androidx.room.TypeConverter
import com.example.myzum.model.AccountType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAccountType(value: String): AccountType {
        return AccountType.valueOf(value)
    }

    @TypeConverter
    fun accountTypeToString(type: AccountType): String {
        return type.name
    }
}
