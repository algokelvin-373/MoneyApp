package com.kelvinht.moneylife.data.repository

import androidx.room.withTransaction
import com.kelvinht.moneylife.data.local.MoneyLifeDatabase
import com.kelvinht.moneylife.domain.repository.TransactionRunner

/**
 * [TransactionRunner] backed by Room. Every DAO call issued inside the block
 * joins the same database transaction, so a crash can never leave half of the
 * writes applied.
 */
class RoomTransactionRunner(
    private val database: MoneyLifeDatabase,
) : TransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T = database.withTransaction { block() }
}
