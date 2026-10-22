package com.kelvinht.moneylife.domain.repository

/**
 * Runs a group of repository calls atomically.
 *
 * The domain layer stays free of persistence details while still being able to
 * guarantee that related writes are either fully applied or fully rolled back,
 * for example saving an expense together with the matching daily saving.
 */
interface TransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
