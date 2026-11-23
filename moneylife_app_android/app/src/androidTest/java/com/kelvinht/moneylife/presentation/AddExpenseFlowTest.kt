package com.kelvinht.moneylife.presentation

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.data.local.MoneyLifeDatabase
import com.kelvinht.moneylife.data.local.entity.ExpenseEntity
import com.kelvinht.moneylife.presentation.addedit.AddExpenseActivity
import com.kelvinht.moneylife.presentation.home.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the flow between the home screen and the add expense screen: the
 * button opens the form, the selected day is handed over through
 * [AddExpenseActivity.EXTRA_DATE_KEY], and a saved expense comes back into the
 * home screen through the database.
 */
@RunWith(AndroidJUnit4::class)
class AddExpenseFlowTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val database = MoneyLifeDatabase.getInstance(context)

    @Before
    fun clearDatabase() {
        val sqlite = database.openHelper.writableDatabase
        sqlite.execSQL("DELETE FROM expenses")
        sqlite.execSQL("DELETE FROM daily_savings")
        sqlite.execSQL("DELETE FROM app_state")
    }

    @Test
    fun tappingTheFab_opensTheAddExpenseScreen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            openAddExpenseForm(scenario)

            onView(withId(R.id.inputAmount)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonSave)).check(matches(isDisplayed()))
            onView(withText("TANGGAL TRANSAKSI")).check(doesNotExist())
            onView(withText("Ubah")).check(doesNotExist())
        }
    }

    @Test
    fun savingAnExpense_showsItOnTheHomeScreen() {
        val today = DateUtils.todayKey()

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            openAddExpenseForm(scenario)

            onView(withId(R.id.inputAmount)).perform(replaceText("25000"), closeSoftKeyboard())
            onView(withId(R.id.inputNote)).perform(scrollTo(), replaceText("Kopi susu"), closeSoftKeyboard())
            onView(withId(R.id.buttonSave)).perform(click())

            // The home screen reads the data back from the database, so the new
            // expense and the new total show up without an explicit result.
            await("total pengeluaran diperbarui") {
                onView(withId(R.id.textSpent)).check(matches(withText("Rp 25.000")))
            }
            onView(withId(R.id.emptyState)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        }

        val stored = storedExpenses(today)
        assertEquals(1, stored.size)
        assertEquals(25_000L, stored.first().amount)
        assertEquals("Kopi susu", stored.first().note)
    }

    @Test
    fun savingOnTheDayFromTheExtra_storesTheExpenseOnThatDay() {
        val yesterday = DateUtils.dateKey(DateUtils.parseDate(DateUtils.todayKey()).minusDays(1))
        val intent =
            Intent(context, AddExpenseActivity::class.java)
                .putExtra(AddExpenseActivity.EXTRA_DATE_KEY, yesterday)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<AddExpenseActivity>(intent).use {
            onView(withId(R.id.inputAmount)).perform(replaceText("15000"), closeSoftKeyboard())
            onView(withId(R.id.buttonSave)).perform(click())
        }

        val stored = awaitStoredExpenses(yesterday)
        assertEquals(1, stored.size)
        assertEquals(15_000L, stored.first().amount)
    }

    /**
     * Scrolls the home screen down until the "Catat Pengeluaran" button is
     * visible and opens the form. The screen is taller than the display, so the
     * button starts below the fold.
     */
    private fun openAddExpenseForm(scenario: ActivityScenario<MainActivity>) {
        await("tombol Catat Pengeluaran terlihat") {
            scenario.onActivity { activity ->
                val scroll =
                    activity.findViewById<ViewGroup>(android.R.id.content)
                        .findNestedScrollView()
                if (scroll != null && scroll.height > 0) {
                    scroll.fullScroll(View.FOCUS_DOWN)
                }
            }
            onView(withId(R.id.fabAdd)).check(matches(isDisplayed()))
        }
        onView(withId(R.id.fabAdd)).perform(click())
    }

    private fun storedExpenses(dateKey: String): List<ExpenseEntity> = runBlocking { database.expenseDao().getByDate(dateKey) }

    private fun awaitStoredExpenses(dateKey: String): List<ExpenseEntity> {
        await("pengeluaran tersimpan di $dateKey") {
            assertTrue(storedExpenses(dateKey).isNotEmpty())
        }
        return storedExpenses(dateKey)
    }

    /**
     * Espresso does not retry assertions, while the database read and the
     * StateFlow update finish asynchronously after the screen is saved.
     */
    private fun await(
        description: String,
        timeoutMillis: Long = TIMEOUT_MILLIS,
        assertion: () -> Unit,
    ) {
        val deadline = System.currentTimeMillis() + timeoutMillis
        var lastFailure: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                assertion()
                return
            } catch (failure: Throwable) {
                lastFailure = failure
                Thread.sleep(RETRY_DELAY_MILLIS)
            }
        }
        throw AssertionError("Gagal menunggu: $description", lastFailure)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
        const val RETRY_DELAY_MILLIS = 100L
    }
}

/** Finds the first nested scroll view without needing a layout id. */
private fun ViewGroup.findNestedScrollView(): NestedScrollView? {
    for (index in 0 until childCount) {
        val child = getChildAt(index)
        if (child is NestedScrollView) return child
        if (child is ViewGroup) {
            child.findNestedScrollView()?.let { return it }
        }
    }
    return null
}
