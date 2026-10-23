package com.kelvinht.moneylife.presentation.home

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelvinht.moneylife.MoneyLifeApplication
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.databinding.ActivityMainBinding
import com.kelvinht.moneylife.domain.model.CategoryBudget
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.presentation.addedit.AddExpenseActivity
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var expenseAdapter: ExpenseAdapter

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as MoneyLifeApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupList()
        setupActions()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun setupList() {
        expenseAdapter = ExpenseAdapter { item -> confirmDelete(item) }
        binding.listExpenses.layoutManager = LinearLayoutManager(this)
        binding.listExpenses.adapter = expenseAdapter
    }

    private fun setupActions() {
        binding.fabAdd.setOnClickListener {
            startActivity(
                Intent(this, AddExpenseActivity::class.java)
                    .putExtra(AddExpenseActivity.EXTRA_DATE_KEY, viewModel.selectedDateKey)
            )
        }
        binding.textDate.setOnClickListener { showDatePicker() }
        binding.textHistoryDate.setOnClickListener { showDatePicker() }
        binding.buttonPreviousDay.setOnClickListener { viewModel.previousDay() }
        binding.buttonNextDay.setOnClickListener { viewModel.nextDay() }
        binding.buttonToday.setOnClickListener { viewModel.showToday() }
    }

    private fun showDatePicker() {
        val selected = DateUtils.parseDate(viewModel.selectedDateKey)
        DatePickerDialog(this, { _, year, month, day ->
            viewModel.selectDate(DateUtils.dateKey(LocalDate.of(year, month + 1, day)))
        }, selected.year, selected.monthValue - 1, selected.dayOfMonth).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HomeUiState) {
        val dateLabel = DateUtils.displayDate(state.dateKey)
        binding.textDate.text = dateLabel
        binding.textDate.contentDescription = getString(R.string.choose_date) + ": " + dateLabel
        binding.textHistoryDate.text = dateLabel
        binding.textHistoryDate.contentDescription = getString(R.string.choose_date) + ": " + dateLabel
        val isToday = state.dateKey == DateUtils.todayKey()
        binding.textSpendingHeader.text = if (isToday) {
            getString(R.string.spending_header_today)
        } else {
            getString(
                R.string.spending_header_date,
                DateUtils.shortDisplayDate(state.dateKey).uppercase(Locale("id", "ID"))
            )
        }
        binding.buttonNextDay.isEnabled = !isToday
        binding.buttonToday.visibility = if (isToday) View.GONE else View.VISIBLE
        binding.textSpent.text = RupiahFormatter.format(state.spent)
        binding.textLimit.text = getString(R.string.limit_from_format, RupiahFormatter.format(state.limit))
        binding.textLimitShort.text =
            getString(R.string.limit_short_format, RupiahFormatter.formatCompact(state.limit))
        binding.textRemaining.text =
            getString(R.string.remaining_format, RupiahFormatter.format(state.remaining))
        binding.progressDaily.setProgressCompat(state.usagePercent, true)
        binding.textUsage.text = getString(R.string.usage_percent_format, state.usagePercent)
        binding.textAverage.text = RupiahFormatter.format(state.savingsBalance)
        binding.textSavingsLabel.text =
            getString(R.string.savings_balance_format, DateUtils.monthLabel(state.dateKey))
        binding.textCondition.text = getString(
            when (state.budgetStatus) {
                BudgetStatus.SAFE -> R.string.budget_condition_safe
                BudgetStatus.WARNING -> R.string.budget_condition_warning
                BudgetStatus.OVER -> R.string.budget_condition_over
            }
        )
        binding.textMakanAmount.text = categoryUsage(state, ExpenseCategory.MAKAN)
        binding.textTransportAmount.text = categoryUsage(state, ExpenseCategory.TRANSPORT)
        binding.textJajanAmount.text = categoryUsage(state, ExpenseCategory.JAJAN)

        val accentColorRes = when (state.budgetStatus) {
            BudgetStatus.SAFE -> R.color.colorSuccess
            BudgetStatus.WARNING -> R.color.colorWarning
            BudgetStatus.OVER -> R.color.colorError
        }
        val accentColor = ContextCompat.getColor(this, accentColorRes)
        binding.progressDaily.setIndicatorColor(accentColor)
        binding.textRemaining.setTextColor(accentColor)

        if (state.budgetStatus == BudgetStatus.OVER) {
            binding.textOverLimit.visibility = View.VISIBLE
            binding.textOverLimit.text =
                getString(R.string.over_limit_format, RupiahFormatter.format(state.overSpent))
        } else {
            binding.textOverLimit.visibility = View.GONE
        }

        binding.emptyState.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
        expenseAdapter.submitList(state.expenses)
    }

    private fun categoryUsage(state: HomeUiState, category: ExpenseCategory): String {
        val spent = state.spentByCategory[category] ?: 0L
        return getString(
            R.string.category_usage_format,
            RupiahFormatter.format(spent),
            RupiahFormatter.formatCompact(CategoryBudget.limitFor(category))
        )
    }

    private fun confirmDelete(item: ExpenseUiModel) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_confirm_title)
            .setMessage(R.string.delete_confirm_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteExpense(item.id) }
            .show()
    }
}
