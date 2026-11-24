package com.kelvinht.moneylife.data.backup

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.room.withTransaction
import com.kelvinht.moneylife.data.local.MoneyLifeDatabase
import com.kelvinht.moneylife.data.local.dao.ExpenseDao
import com.kelvinht.moneylife.data.local.dao.SavingsDao
import com.kelvinht.moneylife.data.local.entity.AppStateEntity
import com.kelvinht.moneylife.data.local.entity.DailySavingEntity
import com.kelvinht.moneylife.data.local.entity.ExpenseEntity
import com.kelvinht.moneylife.domain.model.DailyBudget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.time.LocalDate

/** Writes a portable CSV snapshot and restores it after a reinstall. */
class CsvBackupManager(
    private val context: Context,
    private val database: MoneyLifeDatabase,
    private val expenseDao: ExpenseDao,
    private val savingsDao: SavingsDao,
) {
    suspend fun hasLocalData(): Boolean =
        withContext(Dispatchers.IO) {
            val expenses = expenseDao.getAll()
            val savings = savingsDao.getAllDailySavings()
            val states = savingsDao.getAllStates()
            expenses.isNotEmpty() ||
                savings.isNotEmpty() ||
                states.any { state ->
                    state.stateKey == DEFAULT_LIMIT_KEY &&
                        state.value.toLongOrNull()?.let { it != DailyBudget.DEFAULT_DAILY_LIMIT } == true
                }
        }

    suspend fun exportSnapshot(todayKey: String) =
        withContext(Dispatchers.IO) {
            val expenses = expenseDao.getAll()
            val savings = savingsDao.getAllDailySavings()
            val states = savingsDao.getAllStates()
            val defaultLimit =
                states.firstOrNull { it.stateKey == DEFAULT_LIMIT_KEY }
                    ?.value
                    ?.toLongOrNull()
                    ?: DailyBudget.DEFAULT_DAILY_LIMIT

            // Do not replace a previous backup with an empty snapshot on first launch
            // after reinstall, before the user has selected the backup to restore.
            val hasUserData =
                expenses.isNotEmpty() || savings.isNotEmpty() || defaultLimit != DailyBudget.DEFAULT_DAILY_LIMIT
            if (!hasUserData) return@withContext

            val savingsByDate = savings.associateBy { it.dateKey }
            val stateByKey = states.associateBy { it.stateKey }
            val dates = sortedSetOf(todayKey)
            expenses.forEach { dates += it.dateKey }
            savings.forEach { dates += it.dateKey }
            states.mapNotNullTo(dates) { state ->
                state.stateKey.takeIf { it.startsWith(DAILY_LIMIT_PREFIX) }
                    ?.removePrefix(DAILY_LIMIT_PREFIX)
            }

            val rows =
                buildList {
                    add(CSV_HEADER)
                    expenses.sortedWith(
                        compareBy<ExpenseEntity> { it.dateKey }
                            .thenBy { it.createdAt }
                            .thenBy { it.id },
                    )
                        .forEach { expense ->
                            add(
                                listOf(
                                    "EXPENSE",
                                    expense.dateKey,
                                    expense.id.toString(),
                                    expense.category,
                                    expense.amount.toString(),
                                    expense.note,
                                    expense.createdAt.toString(),
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                ),
                            )
                        }

                    dates.forEach { dateKey ->
                        val dailyLimit =
                            stateByKey["$DAILY_LIMIT_PREFIX$dateKey"]
                                ?.value
                                ?.toLongOrNull()
                                ?: defaultLimit
                        val dailySpent =
                            expenses.asSequence()
                                .filter { it.dateKey == dateKey }
                                .sumOf { it.amount }
                        add(
                            listOf(
                                "DAILY",
                                dateKey,
                                "",
                                "",
                                "",
                                "",
                                "",
                                dailyLimit.toString(),
                                dailySpent.toString(),
                                (dailyLimit - dailySpent).coerceAtLeast(0L).toString(),
                                savingsByDate[dateKey]?.amount?.toString().orEmpty(),
                                "",
                                "",
                            ),
                        )
                    }

                    states.forEach { state ->
                        add(
                            listOf(
                                "STATE",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                state.stateKey,
                                state.value,
                            ),
                        )
                    }
                }

            val csv = rows.joinToString(separator = "\r\n") { row -> row.joinToString(",") { it.escapeCsv() } } + "\r\n"
            writeBackup(csv)
        }

    suspend fun restore(uri: Uri): Int =
        withContext(Dispatchers.IO) {
            val input =
                context.contentResolver.openInputStream(uri)
                    ?: throw IllegalArgumentException("CSV backup tidak dapat dibuka")
            input.use { stream -> restoreFrom(stream) }
        }

    private suspend fun restoreFrom(input: InputStream): Int {
        val records = input.bufferedReader(Charsets.UTF_8).use { reader -> parseCsv(reader.readText()) }
        require(records.isNotEmpty() && records.first() == CSV_HEADER) {
            "Format CSV bukan backup MoneyLife"
        }

        val expenses = mutableListOf<ExpenseEntity>()
        val savings = mutableMapOf<String, DailySavingEntity>()
        val states = mutableMapOf<String, AppStateEntity>()

        records.drop(1).forEachIndexed { index, record ->
            if (record.all(String::isBlank)) return@forEachIndexed
            require(record.size == CSV_HEADER.size) { "Format baris CSV tidak valid: ${index + 2}" }
            when (record[0]) {
                "EXPENSE" -> {
                    val dateKey = record[1]
                    LocalDate.parse(dateKey)
                    expenses +=
                        ExpenseEntity(
                            id =
                                record[2].toLongOrNull()?.takeIf { it > 0L }
                                    ?: throw IllegalArgumentException(
                                        "ID pengeluaran tidak valid pada baris ${index + 2}",
                                    ),
                            category =
                                record[3].takeIf(String::isNotBlank)
                                    ?: throw IllegalArgumentException(
                                        "Kategori kosong pada baris ${index + 2}",
                                    ),
                            amount =
                                record[4].toLongOrNull()?.takeIf { it > 0L }
                                    ?: throw IllegalArgumentException(
                                        "Jumlah pengeluaran tidak valid pada baris ${index + 2}",
                                    ),
                            note = record[5],
                            dateKey = dateKey,
                            createdAt =
                                record[6].toLongOrNull()
                                    ?: throw IllegalArgumentException(
                                        "Waktu pengeluaran tidak valid pada baris ${index + 2}",
                                    ),
                        )
                }

                "DAILY" -> {
                    val dateKey = record[1]
                    LocalDate.parse(dateKey)
                    val limit =
                        record[7].toLongOrNull()?.takeIf { it > 0L }
                            ?: throw IllegalArgumentException(
                                "Limit harian tidak valid pada baris ${index + 2}",
                            )
                    val key = "$DAILY_LIMIT_PREFIX$dateKey"
                    states[key] = AppStateEntity(key, limit.toString())
                    record[10].toLongOrNull()?.let { amount ->
                        require(amount >= 0L) { "Tabungan tidak valid pada baris ${index + 2}" }
                        savings[dateKey] = DailySavingEntity(dateKey, amount)
                    }
                }

                "STATE" -> {
                    val key =
                        record[11].takeIf(String::isNotBlank)
                            ?: throw IllegalArgumentException("State kosong pada baris ${index + 2}")
                    when {
                        key == DEFAULT_LIMIT_KEY ->
                            require(record[12].toLongOrNull()?.let { it > 0L } == true) {
                                "Limit default tidak valid pada baris ${index + 2}"
                            }

                        key == LAST_ACTIVE_DATE_KEY -> LocalDate.parse(record[12])

                        key.startsWith(DAILY_LIMIT_PREFIX) -> {
                            LocalDate.parse(key.removePrefix(DAILY_LIMIT_PREFIX))
                            require(record[12].toLongOrNull()?.let { it > 0L } == true) {
                                "Limit harian tidak valid pada baris ${index + 2}"
                            }
                        }

                        else -> throw IllegalArgumentException("State CSV tidak dikenal pada baris ${index + 2}")
                    }
                    states[key] = AppStateEntity(key, record[12])
                }

                else -> throw IllegalArgumentException("Tipe baris tidak dikenal pada baris ${index + 2}")
            }
        }
        require(expenses.isNotEmpty() || savings.isNotEmpty() || states.isNotEmpty()) {
            "File CSV tidak berisi data MoneyLife"
        }

        database.withTransaction {
            expenseDao.insertAll(expenses)
            if (savings.isNotEmpty()) savingsDao.saveDailySavings(savings.values.toList())
            if (states.isNotEmpty()) savingsDao.saveStates(states.values.toList())
        }
        return expenses.size
    }

    private fun writeBackup(csv: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeMediaStoreBackup(csv)
        } else {
            writeLegacyBackup(csv)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeMediaStoreBackup(csv: String) {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val existing =
            resolver.query(
                collection,
                arrayOf(MediaStore.MediaColumns._ID),
                selection,
                arrayOf(FILE_NAME, RELATIVE_PATH),
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) ContentUris.withAppendedId(collection, cursor.getLong(0)) else null
            }
        val uri =
            existing ?: resolver.insert(
                collection,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH)
                },
            ) ?: throw IllegalStateException("Tidak dapat membuat file backup CSV")

        val output =
            resolver.openOutputStream(uri, "wt")
                ?: throw IllegalStateException("Tidak dapat menulis file backup CSV")
        output.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
    }

    @Suppress("DEPRECATION")
    private fun writeLegacyBackup(csv: String) {
        val directory =
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                FOLDER_NAME,
            )
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("Tidak dapat membuat folder backup MoneyLife")
        }
        File(directory, FILE_NAME).writeText(csv, Charsets.UTF_8)
    }

    private fun String.escapeCsv(): String = "\"${replace("\"", "\"\"")}\""

    private fun parseCsv(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var insideQuotes = false
        var index = 0

        while (index < csv.length) {
            val char = csv[index]
            when {
                insideQuotes && char == '"' && index + 1 < csv.length && csv[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }

                char == '"' -> insideQuotes = !insideQuotes
                !insideQuotes && char == ',' -> {
                    row += field.toString()
                    field.clear()
                }

                !insideQuotes && char == '\n' -> {
                    row += field.toString().removeSuffix("\r")
                    rows += row.toList()
                    row.clear()
                    field.clear()
                }

                else -> field.append(char)
            }
            index++
        }

        if (insideQuotes) throw IllegalArgumentException("CSV memiliki tanda kutip yang tidak lengkap")
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row += field.toString().removeSuffix("\r")
            rows += row.toList()
        }
        return rows
    }

    private companion object {
        const val FILE_NAME = "MoneyLife_Backup.csv"
        const val FOLDER_NAME = "MoneyLife"
        val RELATIVE_PATH = "${Environment.DIRECTORY_DOWNLOADS}/$FOLDER_NAME/"
        const val DEFAULT_LIMIT_KEY = "daily_limit"
        const val DAILY_LIMIT_PREFIX = "daily_limit_"
        const val LAST_ACTIVE_DATE_KEY = "last_active_date"
        val CSV_HEADER =
            listOf(
                "record_type",
                "date_key",
                "expense_id",
                "category",
                "amount",
                "note",
                "created_at",
                "daily_limit",
                "total_spent",
                "remaining",
                "savings",
                "state_key",
                "state_value",
            )
    }
}
