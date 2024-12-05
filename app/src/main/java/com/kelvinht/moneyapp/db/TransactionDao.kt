package com.kelvinht.moneyapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kelvinht.moneyapp.data.MoneyIn


@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions_data")
    suspend fun getTransactionsByDate(): List<MoneyIn>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(moneyIn: MoneyIn)

    @Update
    suspend fun updateMoneyIn(moneyIn: MoneyIn)

    @Delete
    fun deleteMoneyIn(moneyIn: MoneyIn)
}
