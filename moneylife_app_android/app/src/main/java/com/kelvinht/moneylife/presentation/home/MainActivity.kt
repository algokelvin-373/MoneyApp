package com.kelvinht.moneylife.presentation.home

import android.Manifest
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.presentation.addedit.AddExpenseActivity
import com.kelvinht.moneylife.presentation.privacy.PrivacyPolicyActivity
import com.kelvinht.moneylife.presentation.summary.MonthlySummaryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categorySummaryAdapter: CategorySummaryAdapter

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as MoneyLifeApplication).container)
    }

    private val backupFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) restoreBackup(uri)
        }

    private val legacyStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.refresh()
            } else {
                Toast.makeText(this, R.string.csv_backup_storage_permission_required, Toast.LENGTH_LONG).show()
            }
            showRestorePromptIfNeeded()
        }

    /**
     * Keeps the home screen in sync when the user fixes the device clock while
     * the app is open. Without this the date picked at startup would stay
     * visible until the app is backgrounded and resumed.
     */
    private val clockChangeReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                viewModel.refresh()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupList()
        setupActions()
        observeState()
        prepareCsvBackup()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            clockChangeReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(clockChangeReceiver)
    }

    private fun setupList() {
        expenseAdapter =
            ExpenseAdapter(
                onEditClick = { item ->
                    startActivity(
                        Intent(this, AddExpenseActivity::class.java)
                            .putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, item.id),
                    )
                },
                onDeleteClick = { item -> confirmDelete(item) },
            )
        binding.listExpenses.layoutManager = LinearLayoutManager(this)
        binding.listExpenses.adapter = expenseAdapter

        categorySummaryAdapter = CategorySummaryAdapter()
        binding.listPopularCategories.layoutManager = LinearLayoutManager(this)
        binding.listPopularCategories.adapter = categorySummaryAdapter
        binding.listPopularCategories.isNestedScrollingEnabled = false
    }

    private fun setupActions() {
        binding.fabAdd.setOnClickListener {
            startActivity(
                Intent(this, AddExpenseActivity::class.java)
                    .putExtra(AddExpenseActivity.EXTRA_DATE_KEY, viewModel.selectedDateKey),
            )
        }
        binding.textDate.setOnClickListener { showDatePicker() }
        binding.textHistoryDate.setOnClickListener { showDatePicker() }
        binding.buttonPreviousDay.setOnClickListener { viewModel.previousDay() }
        binding.buttonNextDay.setOnClickListener { viewModel.nextDay() }
        binding.buttonToday.setOnClickListener { viewModel.showToday() }
        binding.buttonLimit50.setOnClickListener {
            viewModel.selectDailyLimit(DailyBudget.DAILY_LIMIT_50K)
        }
        binding.buttonLimit100.setOnClickListener {
            viewModel.selectDailyLimit(DailyBudget.DAILY_LIMIT_100K)
        }
        binding.buttonLimit150.setOnClickListener {
            viewModel.selectDailyLimit(DailyBudget.DAILY_LIMIT_150K)
        }
        binding.buttonLimit200.setOnClickListener {
            viewModel.selectDailyLimit(DailyBudget.DAILY_LIMIT_200K)
        }
        binding.buttonLimitCustom.setOnClickListener { showCustomLimitDialog() }
        binding.buttonPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
        binding.buttonMonthlySummary.setOnClickListener {
            startActivity(Intent(this, MonthlySummaryActivity::class.java))
        }
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

    private fun prepareCsvBackup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            legacyStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            showRestorePromptIfNeeded()
        }
    }

    private fun showRestorePromptIfNeeded() {
        lifecycleScope.launch {
            val hasLocalData =
                withContext(Dispatchers.IO) {
                    (application as MoneyLifeApplication).container.csvBackupManager.hasLocalData()
                }
            val hasBackupFile =
                withContext(Dispatchers.IO) {
                    (application as MoneyLifeApplication).container.csvBackupManager.hasBackupFile()
                }
            val dismissed =
                getSharedPreferences(BACKUP_PREFERENCES, MODE_PRIVATE)
                    .getBoolean(RESTORE_PROMPT_DISMISSED, false)
            if (hasBackupFile && !hasLocalData && !dismissed && !isFinishing) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(R.string.csv_backup_restore_title)
                    .setMessage(R.string.csv_backup_restore_message)
                    .setPositiveButton(R.string.csv_backup_restore_action) { _, _ ->
                        backupFilePicker.launch(arrayOf("*/*"))
                    }
                    .setNegativeButton(R.string.csv_backup_later) { _, _ ->
                        getSharedPreferences(BACKUP_PREFERENCES, MODE_PRIVATE)
                            .edit()
                            .putBoolean(RESTORE_PROMPT_DISMISSED, true)
                            .apply()
                    }
                    .show()
            }
        }
    }

    private fun restoreBackup(uri: Uri) {
        val content =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(48, 32, 48, 32)
                addView(
                    ProgressBar(this@MainActivity),
                    LinearLayout.LayoutParams(48, 48),
                )
                addView(
                    TextView(this@MainActivity).apply {
                        text = getString(R.string.csv_backup_processing)
                        setPadding(32, 0, 0, 0)
                    },
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
                )
            }
        val processingDialog =
            AlertDialog.Builder(this)
                .setView(content)
                .setCancelable(false)
                .create()
        processingDialog.show()

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    (application as MoneyLifeApplication).container.csvBackupManager.restore(uri)
                }
            }.onSuccess { importedCount ->
                processingDialog.dismiss()
                getSharedPreferences(BACKUP_PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putBoolean(RESTORE_PROMPT_DISMISSED, true)
                    .apply()
                viewModel.refresh()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.csv_backup_restore_success, importedCount),
                    Toast.LENGTH_LONG,
                ).show()
            }.onFailure { error ->
                processingDialog.dismiss()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.csv_backup_restore_error, error.message.orEmpty()),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    private fun render(state: HomeUiState) {
        val dateLabel = DateUtils.displayDate(state.dateKey)
        binding.textDate.text = dateLabel
        binding.textDate.contentDescription = getString(R.string.choose_date) + ": " + dateLabel
        binding.textHistoryDate.text = dateLabel
        binding.textHistoryDate.contentDescription =
            getString(R.string.choose_date) + ": " + dateLabel
        val isToday = state.dateKey == DateUtils.todayKey()
        val canChangeLimit = isToday
        binding.textSpendingHeader.text =
            if (isToday) {
                getString(R.string.spending_header_today)
            } else {
                getString(
                    R.string.spending_header_date,
                    DateUtils.shortDisplayDate(state.dateKey).uppercase(Locale("id", "ID")),
                )
            }
        binding.buttonNextDay.isEnabled = !isToday
        binding.buttonToday.visibility = if (isToday) View.GONE else View.VISIBLE
        binding.textSpent.text = RupiahFormatter.format(state.spent)
        binding.textLimit.text =
            getString(R.string.limit_from_format, RupiahFormatter.format(state.limit))
        binding.textLimitShort.text =
            getString(
                if (canChangeLimit) R.string.limit_short_format else R.string.limit_short_locked_format,
                RupiahFormatter.formatCompact(state.limit),
            )
        renderLimitOptions(state.limit, enabled = canChangeLimit)
        binding.textRemaining.text =
            getString(R.string.remaining_format, RupiahFormatter.format(state.remaining))
        binding.progressDaily.setProgressCompat(state.usagePercent, true)
        binding.textUsage.text = getString(R.string.usage_percent_format, state.usagePercent)
        binding.textSavings.text = RupiahFormatter.format(state.savingsBalance)
        binding.textSavingsLabel.text =
            getString(R.string.savings_balance_format, DateUtils.monthLabel(state.dateKey))
        binding.textCondition.text =
            getString(
                when (state.budgetStatus) {
                    BudgetStatus.SAFE -> R.string.budget_condition_safe
                    BudgetStatus.WARNING -> R.string.budget_condition_warning
                    BudgetStatus.OVER -> R.string.budget_condition_over
                },
            )
        categorySummaryAdapter.submitList(
            ExpenseCategory.entries.map { category ->
                CategorySummaryUiModel(
                    category = category,
                    spent = state.spentByCategory[category] ?: 0L,
                )
            },
        )

        val accentColorRes =
            when (state.budgetStatus) {
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

    private fun showCustomLimitDialog() {
        val input =
            EditText(this).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                hint = getString(R.string.custom_limit_hint)
                setSingleLine(true)
                viewModel.uiState.value.limit
                    .takeIf { it !in DailyBudget.DAILY_LIMIT_OPTIONS }
                    ?.let { setText(it.toString()) }
            }
        val dialog =
            AlertDialog.Builder(this)
                .setTitle(R.string.custom_limit_title)
                .setMessage(R.string.custom_limit_message)
                .setView(input)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save_limit, null)
                .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val limit = input.text?.toString()?.trim()?.toLongOrNull()
                if (limit == null || limit <= 0L) {
                    input.error = getString(R.string.custom_limit_invalid)
                } else {
                    viewModel.selectDailyLimit(limit)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun renderLimitOptions(
        limit: Long,
        enabled: Boolean,
    ) {
        updateLimitOption(
            binding.buttonLimit50,
            selected = limit == DailyBudget.DAILY_LIMIT_50K,
            enabled = enabled,
        )
        updateLimitOption(
            binding.buttonLimit100,
            selected = limit == DailyBudget.DAILY_LIMIT_100K,
            enabled = enabled,
        )
        updateLimitOption(
            binding.buttonLimit150,
            selected = limit == DailyBudget.DAILY_LIMIT_150K,
            enabled = enabled,
        )
        updateLimitOption(
            binding.buttonLimit200,
            selected = limit == DailyBudget.DAILY_LIMIT_200K,
            enabled = enabled,
        )
        updateLimitOption(
            binding.buttonLimitCustom,
            selected = limit !in DailyBudget.DAILY_LIMIT_OPTIONS,
            enabled = enabled,
        )
    }

    private fun updateLimitOption(
        option: TextView,
        selected: Boolean,
        enabled: Boolean,
    ) {
        option.isSelected = selected
        option.isEnabled = enabled
        option.alpha = if (enabled) 1f else DISABLED_LIMIT_ALPHA
        option.setBackgroundResource(
            if (selected) R.drawable.bg_limit_option else android.R.color.transparent,
        )
        option.setTextColor(
            ContextCompat.getColor(
                this,
                if (selected) R.color.colorPrimaryDark else R.color.textSecondary,
            ),
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

    private companion object {
        const val DISABLED_LIMIT_ALPHA = 0.45f
        const val BACKUP_PREFERENCES = "csv_backup_preferences"
        const val RESTORE_PROMPT_DISMISSED = "restore_prompt_dismissed"
    }
}
