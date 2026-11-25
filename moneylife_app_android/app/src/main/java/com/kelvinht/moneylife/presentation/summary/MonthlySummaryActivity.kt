package com.kelvinht.moneylife.presentation.summary

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelvinht.moneylife.MoneyLifeApplication
import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.databinding.ActivityMonthlySummaryBinding
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.presentation.home.CategorySummaryAdapter
import com.kelvinht.moneylife.presentation.home.CategorySummaryUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

class MonthlySummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMonthlySummaryBinding
    private lateinit var categoryAdapter: CategorySummaryAdapter
    private val latestCompletedMonth: YearMonth = YearMonth.now().minusMonths(1)
    private var selectedMonth: YearMonth = latestCompletedMonth
    private var summaryJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryAdapter = CategorySummaryAdapter()
        binding.listCategories.layoutManager = LinearLayoutManager(this)
        binding.listCategories.adapter = categoryAdapter

        binding.buttonBack.setOnClickListener { finish() }
        binding.buttonPreviousMonth.setOnClickListener {
            selectedMonth = selectedMonth.minusMonths(1)
            loadSummary()
        }
        binding.buttonNextMonth.setOnClickListener {
            if (selectedMonth.isBefore(latestCompletedMonth)) {
                selectedMonth = selectedMonth.plusMonths(1)
                loadSummary()
            }
        }

        loadSummary()
    }

    private fun loadSummary() {
        val month = selectedMonth
        val monthKey = month.toString()
        binding.textMonth.text = DateUtils.monthLabel(month.atDay(1).toString())
        binding.buttonNextMonth.isEnabled = selectedMonth.isBefore(latestCompletedMonth)
        binding.buttonNextMonth.alpha = if (binding.buttonNextMonth.isEnabled) 1f else 0.4f

        summaryJob?.cancel()
        summaryJob = lifecycleScope.launch {
            val summary = withContext(Dispatchers.IO) {
                (application as MoneyLifeApplication).container.getMonthlyExpenseSummary(monthKey)
            }
            binding.textTotalExpenses.text = RupiahFormatter.format(summary.totalExpenses)
            binding.textTotalSavings.text = RupiahFormatter.format(summary.totalSavings)
            categoryAdapter.submitList(
                ExpenseCategory.values().map { category ->
                    CategorySummaryUiModel(category, summary.categoryTotals[category] ?: 0L)
                },
            )
            binding.textEmpty.visibility =
                if (summary.totalExpenses == 0L && summary.totalSavings == 0L) View.VISIBLE else View.GONE
        }
    }
}
