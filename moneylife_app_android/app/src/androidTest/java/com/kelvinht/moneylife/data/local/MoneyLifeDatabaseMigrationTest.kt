package com.kelvinht.moneylife.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Protects the users that already run schema version 1: the migration has to
 * keep every recorded expense and create the new tables.
 *
 * The schemas of both versions are exported to `app/schemas` and shipped as
 * assets of the test APK, so the result of the migration is validated against
 * the real schema history instead of a hand written expectation.
 */
@RunWith(AndroidJUnit4::class)
class MoneyLifeDatabaseMigrationTest {
    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            MoneyLifeDatabase::class.java,
        )

    @Test
    fun migrateFromVersion1To2_keepsTheExpensesAndCreatesTheNewTables() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO expenses (category, amount, note, dateKey, createdAt) " +
                    "VALUES ('MAKAN', 30000, 'Kopi', '2026-10-11', 1000)",
            )
            close()
        }

        val migrated: SupportSQLiteDatabase =
            helper.runMigrationsAndValidate(
                TEST_DATABASE,
                2,
                true,
                MoneyLifeDatabase.MIGRATION_1_2,
            )

        migrated.query("SELECT amount, note FROM expenses").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(30_000L, cursor.getLong(0))
            assertEquals("Kopi", cursor.getString(1))
            assertEquals(1, cursor.count)
        }

        migrated.query("SELECT COUNT(*) FROM daily_savings").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        migrated.query("SELECT COUNT(*) FROM app_state").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        migrated.close()
    }

    @Test
    fun migrateFromVersion1To2_canStoreTheSavingsAndTheAppState() {
        helper.createDatabase(TEST_DATABASE, 1).close()

        val migrated: SupportSQLiteDatabase =
            helper.runMigrationsAndValidate(
                TEST_DATABASE,
                2,
                true,
                MoneyLifeDatabase.MIGRATION_1_2,
            )

        migrated.execSQL("INSERT INTO daily_savings (dateKey, amount) VALUES ('2026-10-11', 70000)")
        migrated.execSQL("INSERT INTO app_state (stateKey, value) VALUES ('last_active_date', '2026-10-11')")

        migrated.query("SELECT amount FROM daily_savings WHERE dateKey = '2026-10-11'")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(70_000L, cursor.getLong(0))
            }

        migrated.query("SELECT value FROM app_state WHERE stateKey = 'last_active_date'")
            .use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("2026-10-11", cursor.getString(0))
            }

        migrated.close()
    }

    private companion object {
        const val TEST_DATABASE = "migration-test"
    }
}
