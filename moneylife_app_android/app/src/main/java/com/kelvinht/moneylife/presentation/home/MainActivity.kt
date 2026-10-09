package com.kelvinht.moneylife.presentation.home

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.kelvinht.moneylife.databinding.ActivityMainBinding
import com.kelvinht.moneylife.presentation.addedit.AddExpenseActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var expenseAdapter: ExpenseAdapter

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((application as MoneyLifeApplication).container)
    }

    private val addExpenseLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.refresh()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupList()
        setupActions()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupList() {
        expenseAdapter = ExpenseAdapter { item -> confirmDelete(item) }
        binding.listExpenses.layoutManager = LinearLayoutManager(this)
        binding.listExpenses.adapter = expenseAdapter
    }

    private fun setupActions() {
        binding.fabAdd.setOnClickListener {
            addExpenseLauncher.launch(Intent(this, AddExpenseActivity::class.java))
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: HomeUiState) {
        binding.textDate.text = state.dateLabel
        binding.textSpent.text = state.spentText
        binding.textLimit.text = state.limitText
        binding.textRemaining.text = state.remainingText
        binding.progressDaily.setProgressCompat(state.usagePercent, true)

        val accentColorRes = when (state.budgetStatus) {
            BudgetStatus.SAFE -> R.color.colorSuccess
            BudgetStatus.WARNING -> R.color.colorWarning
            BudgetStatus.OVER -> R.color.colorError
        }
        val accentColor = ContextCompat.getColor(this, accentColorRes)
        binding.progressDaily.setIndicatorColor(accentColor)
        binding.textRemaining.setTextColor(accentColor)

        val overLimitText = state.overLimitText
        if (overLimitText != null) {
            binding.textOverLimit.visibility = View.VISIBLE
            binding.textOverLimit.text = overLimitText
        } else {
            binding.textOverLimit.visibility = View.GONE
        }

        binding.emptyState.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
        expenseAdapter.submitList(state.expenses)
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
