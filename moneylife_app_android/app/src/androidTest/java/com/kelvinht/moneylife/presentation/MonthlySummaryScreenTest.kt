package com.kelvinht.moneylife.presentation

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.presentation.summary.MonthlySummaryActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MonthlySummaryScreenTest {
    @Test
    fun currentMonthCanBeOpenedAndReportExportIsAvailable() {
        ActivityScenario.launch(MonthlySummaryActivity::class.java).use {
            onView(withId(R.id.buttonCurrentMonth)).check(matches(isDisplayed())).perform(click())
            onView(withId(R.id.textMonth)).check(matches(withText(DateUtils.monthLabel(DateUtils.todayKey()))))
            onView(withId(R.id.chartDailySpending)).check(matches(isDisplayed()))
            onView(withId(R.id.buttonExport)).check(matches(isDisplayed()))
        }
    }
}
