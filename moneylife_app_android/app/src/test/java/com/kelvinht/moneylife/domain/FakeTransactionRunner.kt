package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.repository.TransactionRunner

/**
 * Test double that runs the block and records whether it was executed inside a
 * transaction. Repositories can assert [inTransaction] to prove that a write
 * happened atomically together with its siblings.
 */
class FakeTransactionRunner : TransactionRunner {
    var transactions = 0
        private set

    var inTransaction = false
        private set

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        transactions++
        inTransaction = true
        try {
            return block()
        } finally {
            inTransaction = false
        }
    }
}
