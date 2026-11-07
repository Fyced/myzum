package com.example.myzum.repository.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PaymentEntity::class, UserEntity::class, FundingSourceEntity::class, ProductEntity::class, RefundEntity::class], 
    version = 9, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao
    abstract fun userDao(): UserDao
    abstract fun fundingSourceDao(): FundingSourceDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun productDao(): ProductDao
    abstract fun refundDao(): RefundDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myzum_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
