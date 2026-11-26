package com.kelvinht.moneylife.presentation.summary

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelvinht.moneylife.MoneyLifeApplication
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.export.MonthlySummaryCsvExporter
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
import java.nio.charset.StandardCharsets
import java.time.YearMonth

class MonthlySummaryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMonthlySummaryBinding
    private lateinit var categoryAdapter: CategorySummaryAdapter
    private val currentMonth: YearMonth = YearMonth.now()
    private val latestCompletedMonth: YearMonth = currentMonth.minusMonths(1)
    private var selectedMonth: YearMonth = latestCompletedMonth
    private var exportMonthKey: String = latestCompletedMonth.toString()
    private var summaryJob: Job? = null

    private val createReportDocument =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) exportReport(uri, exportMonthKey)
        }

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
            if (selectedMonth.isBefore(currentMonth)) {
                selectedMonth = selectedMonth.plusMonths(1)
                loadSummary()
            }
        }
        binding.buttonCurrentMonth.setOnClickListener {
            selectedMonth = currentMonth
            loadSummary()
        }
        binding.buttonExport.setOnClickListener {
            exportMonthKey = selectedMonth.toString()
            createReportDocument.launch(getString(R.string.monthly_report_file_name, exportMonthKey))
        }

        loadSummary()
    }

    private fun loadSummary() {
        val month = selectedMonth
        val monthKey = month.toString()
        binding.textMonth.text = DateUtils.monthLabel(month.atDay(1).toString())
        binding.buttonNextMonth.isEnabled = selectedMonth.isBefore(currentMonth)
        binding.buttonNextMonth.alpha = if (binding.buttonNextMonth.isEnabled) 1f else 0.4f
        binding.buttonCurrentMonth.visibility = if (month == currentMonth) View.GONE else View.VISIBLE

        summaryJob?.cancel()
        summaryJob =
            lifecycleScope.launch {
                val summary =
                    withContext(Dispatchers.IO) {
                        (application as MoneyLifeApplication).container.getMonthlyExpenseSummary(monthKey)
                    }
                binding.textTotalExpenses.text = RupiahFormatter.format(summary.totalExpenses)
                binding.textTotalSavings.text = RupiahFormatter.format(summary.totalSavings)
                binding.chartDailySpending.setMonthlyData(month, summary.dailyTotals)
                categoryAdapter.submitList(
                    ExpenseCategory.values().map { category ->
                        CategorySummaryUiModel(category, summary.categoryTotals[category] ?: 0L)
                    },
                )
                binding.textEmpty.visibility =
                    if (summary.totalExpenses == 0L && summary.totalSavings == 0L) View.VISIBLE else View.GONE
            }
    }

    private fun exportReport(
        uri: Uri,
        monthKey: String,
    ) {
        lifecycleScope.launch {
            val result =
                runCatching {
                    withContext(Dispatchers.IO) {
                        val summary =
                            (application as MoneyLifeApplication).container.getMonthlyExpenseSummary(monthKey)
                        val csv = MonthlySummaryCsvExporter.toCsv(monthKey, summary)
                        val outputStream = contentResolver.openOutputStream(uri) ?: error("Lokasi file tidak tersedia")
                        outputStream.use { it.write(csv.toByteArray(StandardCharsets.UTF_8)) }
                    }
                }
            val message = if (result.isSuccess) R.string.monthly_export_success else R.string.monthly_export_error
            Toast.makeText(this@MonthlySummaryActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}
