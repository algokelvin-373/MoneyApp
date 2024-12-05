package com.kelvinht.moneyapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelvinht.moneyapp.data.Transaction


@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions_data")
    suspend fun getTransactionsByDate(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: Transaction)

    @Delete
    fun deleteTransaction(transaction: Transaction)
}
