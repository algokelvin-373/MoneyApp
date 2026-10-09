package com.kelvinht.moneylife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kelvinht.moneylife.data.local.dao.ExpenseDao
import com.kelvinht.moneylife.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MoneyLifeDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val NAME = "moneylife.db"

        @Volatile
        private var instance: MoneyLifeDatabase? = null

        fun getInstance(context: Context): MoneyLifeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MoneyLifeDatabase::class.java,
                    NAME
                ).build().also { instance = it }
            }
    }
}
