package com.kelvinht.moneylife.presentation.addedit

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.card.MaterialCardView
import com.kelvinht.moneylife.MoneyLifeApplication
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.databinding.ActivityAddExpenseBinding
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import kotlinx.coroutines.launch

class AddExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var categoryOptions: List<CategoryOption>

    private val viewModel: AddExpenseViewModel by viewModels {
        AddExpenseViewModelFactory((application as MoneyLifeApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, NO_EXPENSE_ID)
        if (expenseId != NO_EXPENSE_ID) {
            viewModel.startEditing(expenseId)
        } else if (savedInstanceState == null) {
            intent.getStringExtra(EXTRA_DATE_KEY)?.let(viewModel::selectDate)
        }

        setupCategorySelector()
        setupInputs()
        setupSaveButton()
        observeState()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupCategorySelector() {
        categoryOptions =
            listOf(
                CategoryOption(ExpenseCategory.MAKAN, binding.categoryMakan, binding.checkMakan),
                CategoryOption(
                    ExpenseCategory.TRANSPORT,
                    binding.categoryTransport,
                    binding.checkTransport,
                ),
                CategoryOption(ExpenseCategory.JAJAN, binding.categoryJajan, binding.checkJajan),
                CategoryOption(
                    ExpenseCategory.KEBUTUHAN_LAIN,
                    binding.categoryKebutuhanLain,
                    binding.checkKebutuhanLain,
                ),
                CategoryOption(
                    ExpenseCategory.KEBUTUHAN_ORANG_TUA,
                    binding.categoryKebutuhanOrangTua,
                    binding.checkKebutuhanOrangTua,
                ),
                CategoryOption(
                    ExpenseCategory.TOP_UP_COINS_TIKTOK,
                    binding.categoryTopUpCoinsTiktok,
                    binding.checkTopUpCoinsTiktok,
                ),
            )
        categoryOptions.forEach { option ->
            option.card.setOnClickListener { viewModel.selectCategory(option.category) }
        }
    }

    private fun setupInputs() {
        binding.inputAmount.addTextChangedListener { editable ->
            viewModel.onAmountChanged(editable?.toString().orEmpty())
        }
        binding.inputNote.addTextChangedListener { editable ->
            viewModel.onNoteChanged(editable?.toString().orEmpty())
        }
        binding.quick10.setOnClickListener { viewModel.addQuickAmount(10_000) }
        binding.quick20.setOnClickListener { viewModel.addQuickAmount(20_000) }
        binding.quick50.setOnClickListener { viewModel.addQuickAmount(50_000) }
        binding.quick100.setOnClickListener { viewModel.addQuickAmount(100_000) }
        binding.buttonReset.setOnClickListener { viewModel.reset() }
        binding.buttonBack.setOnClickListener { finish() }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener { viewModel.save() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: AddExpenseUiState) {
        val formattedAmount =
            state.amountInput.toLongOrNull()?.let(RupiahFormatter::formatDigits).orEmpty()
        if (binding.inputAmount.text?.toString() != formattedAmount) {
            binding.inputAmount.setText(formattedAmount)
            binding.inputAmount.setSelection(formattedAmount.length)
        }
        if (binding.inputNote.text?.toString() != state.note) {
            binding.inputNote.setText(state.note)
            binding.inputNote.setSelection(state.note.length)
        }
        binding.textTitle.setText(if (state.isEditing) R.string.edit_expense_title else R.string.add_expense)
        binding.inputAmount.isEnabled = !state.isLoadingExpense
        binding.inputNote.isEnabled = !state.isLoadingExpense
        binding.quick10.isEnabled = !state.isLoadingExpense
        binding.quick20.isEnabled = !state.isLoadingExpense
        binding.quick50.isEnabled = !state.isLoadingExpense
        binding.quick100.isEnabled = !state.isLoadingExpense
        renderCategory(state.category, enabled = !state.isLoadingExpense)
        binding.textError.text = state.error?.let(::getString).orEmpty()
        binding.textError.visibility = if (state.error == null) View.GONE else View.VISIBLE

        binding.buttonReset.visibility = if (state.isEditing) View.INVISIBLE else View.VISIBLE
        binding.buttonSave.isEnabled = !state.isSaving && !state.isLoadingExpense
        binding.buttonSave.text =
            getString(
                when {
                    state.isLoadingExpense -> R.string.loading_expense
                    state.isSaving -> R.string.saving
                    state.isEditing -> R.string.edit_expense_save
                    else -> R.string.save
                },
            )

        if (state.isSaved) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun renderCategory(selected: ExpenseCategory, enabled: Boolean) {
        categoryOptions.forEach { option ->
            val isSelected = option.category == selected
            styleCategory(option.card, isSelected)
            option.card.isEnabled = enabled
            option.card.alpha = if (enabled) 1f else 0.6f
            option.checkmark.visibility = if (isSelected) View.VISIBLE else View.GONE
        }
    }

    private data class CategoryOption(
        val category: ExpenseCategory,
        val card: MaterialCardView,
        val checkmark: View,
    )

    private fun styleCategory(
        card: MaterialCardView,
        selected: Boolean,
    ) {
        card.strokeWidth =
            resources.getDimensionPixelSize(if (selected) R.dimen.selected_stroke else R.dimen.default_stroke)
        card.strokeColor =
            ContextCompat.getColor(
                this,
                if (selected) R.color.colorPrimaryDark else R.color.cardBorder,
            )
    }

    companion object {
        const val EXTRA_DATE_KEY = "com.kelvinht.moneylife.DATE_KEY"
        const val EXTRA_EXPENSE_ID = "com.kelvinht.moneylife.EXPENSE_ID"
        private const val NO_EXPENSE_ID = 0L
    }
}
