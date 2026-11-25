package com.kelvinht.moneylife.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelvinht.moneylife.data.local.entity.AppStateEntity
import com.kelvinht.moneylife.data.local.entity.DailySavingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT COALESCE(SUM(amount), 0) FROM daily_savings WHERE substr(dateKey, 1, 7) = :monthKey")
    fun observeBalance(monthKey: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM daily_savings WHERE substr(dateKey, 1, 7) = :monthKey")
    suspend fun getMonthlySavings(monthKey: String): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailySaving(entity: DailySavingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailySavings(entities: List<DailySavingEntity>)

    @Query("SELECT * FROM daily_savings ORDER BY dateKey")
    suspend fun getAllDailySavings(): List<DailySavingEntity>

    @Query("SELECT value FROM app_state WHERE stateKey = :key LIMIT 1")
    suspend fun getState(key: String): String?

    @Query("SELECT value FROM app_state WHERE stateKey = :key LIMIT 1")
    fun observeState(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(entity: AppStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStates(entities: List<AppStateEntity>)

    @Query("SELECT * FROM app_state ORDER BY stateKey")
    suspend fun getAllStates(): List<AppStateEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveStateIfAbsent(entity: AppStateEntity)
}
