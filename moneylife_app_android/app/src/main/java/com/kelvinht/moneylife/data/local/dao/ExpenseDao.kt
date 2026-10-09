package com.kelvinht.moneylife.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelvinht.moneylife.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE dateKey = :dateKey ORDER BY createdAt DESC")
    fun observeByDate(dateKey: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateKey = :dateKey ORDER BY createdAt DESC")
    suspend fun getByDate(dateKey: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ExpenseEntity): Long

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM expenses ORDER BY createdAt DESC")
    suspend fun getAll(): List<ExpenseEntity>
}
