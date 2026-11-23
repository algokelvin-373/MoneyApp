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

    private val viewModel: AddExpenseViewModel by viewModels {
        AddExpenseViewModelFactory((application as MoneyLifeApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
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
        binding.categoryMakan.setOnClickListener { viewModel.selectCategory(ExpenseCategory.MAKAN) }
        binding.categoryTransport.setOnClickListener { viewModel.selectCategory(ExpenseCategory.TRANSPORT) }
        binding.categoryJajan.setOnClickListener { viewModel.selectCategory(ExpenseCategory.JAJAN) }
        binding.categoryKebutuhanLain.setOnClickListener {
            viewModel.selectCategory(ExpenseCategory.KEBUTUHAN_LAIN)
        }
        binding.categoryKebutuhanOrangTua.setOnClickListener {
            viewModel.selectCategory(ExpenseCategory.KEBUTUHAN_ORANG_TUA)
        }
        binding.categoryTopUpCoinsTiktok.setOnClickListener {
            viewModel.selectCategory(ExpenseCategory.TOP_UP_COINS_TIKTOK)
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
        renderCategory(state.category)
        binding.textError.text = state.error?.let(::getString).orEmpty()
        binding.textError.visibility = if (state.error == null) View.GONE else View.VISIBLE

        binding.buttonSave.isEnabled = !state.isSaving
        binding.buttonSave.text = getString(if (state.isSaving) R.string.saving else R.string.save)

        if (state.isSaved) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun renderCategory(selected: ExpenseCategory) {
        val cards =
            listOf(
                binding.categoryMakan to ExpenseCategory.MAKAN,
                binding.categoryTransport to ExpenseCategory.TRANSPORT,
                binding.categoryJajan to ExpenseCategory.JAJAN,
                binding.categoryKebutuhanLain to ExpenseCategory.KEBUTUHAN_LAIN,
                binding.categoryKebutuhanOrangTua to ExpenseCategory.KEBUTUHAN_ORANG_TUA,
                binding.categoryTopUpCoinsTiktok to ExpenseCategory.TOP_UP_COINS_TIKTOK,
            )
        cards.forEach { (card, category) ->
            styleCategory(card, category == selected)
        }
        binding.checkMakan.visibility =
            if (selected == ExpenseCategory.MAKAN) View.VISIBLE else View.GONE
        binding.checkTransport.visibility =
            if (selected == ExpenseCategory.TRANSPORT) View.VISIBLE else View.GONE
        binding.checkJajan.visibility =
            if (selected == ExpenseCategory.JAJAN) View.VISIBLE else View.GONE
        binding.checkKebutuhanLain.visibility =
            if (selected == ExpenseCategory.KEBUTUHAN_LAIN) View.VISIBLE else View.GONE
        binding.checkKebutuhanOrangTua.visibility =
            if (selected == ExpenseCategory.KEBUTUHAN_ORANG_TUA) View.VISIBLE else View.GONE
        binding.checkTopUpCoinsTiktok.visibility =
            if (selected == ExpenseCategory.TOP_UP_COINS_TIKTOK) View.VISIBLE else View.GONE
    }

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
    }
}
