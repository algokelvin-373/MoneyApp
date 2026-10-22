package com.kelvinht.moneylife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kelvinht.moneylife.data.local.dao.SavingsDao
import com.kelvinht.moneylife.data.local.entity.AppStateEntity
import com.kelvinht.moneylife.data.local.entity.DailySavingEntity
import com.kelvinht.moneylife.data.local.dao.ExpenseDao
import com.kelvinht.moneylife.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class, DailySavingEntity::class, AppStateEntity::class],
    version = 2,
    exportSchema = true
)
abstract class MoneyLifeDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun savingsDao(): SavingsDao

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
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }

        /**
         * Migration from schema version 1 (table `expenses`) to version 2
         * (adds `daily_savings` and `app_state`). Public so the migration can
         * be covered by an instrumentation test.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS daily_savings (" +
                        "dateKey TEXT NOT NULL PRIMARY KEY, " +
                        "amount INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS app_state (" +
                        "stateKey TEXT NOT NULL PRIMARY KEY, " +
                        "value TEXT NOT NULL)"
                )
            }
        }
    }
}
