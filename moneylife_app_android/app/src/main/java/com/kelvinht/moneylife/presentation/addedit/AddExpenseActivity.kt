package com.kelvinht.moneylife.presentation.addedit

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kelvinht.moneylife.MoneyLifeApplication
import com.kelvinht.moneylife.R
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

        setupToolbar()
        setupCategorySelector()
        setupInputs()
        setupSaveButton()
        observeState()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    private fun setupCategorySelector() {
        binding.groupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val category = when (checkedId) {
                R.id.chipTransport -> ExpenseCategory.TRANSPORT
                R.id.chipJajan -> ExpenseCategory.JAJAN
                else -> ExpenseCategory.MAKAN
            }
            viewModel.selectCategory(category)
        }
        binding.groupCategory.check(R.id.chipMakan)
    }

    private fun setupInputs() {
        binding.inputAmount.addTextChangedListener { editable ->
            viewModel.onAmountChanged(editable?.toString().orEmpty())
        }
        binding.inputNote.addTextChangedListener { editable ->
            viewModel.onNoteChanged(editable?.toString().orEmpty())
        }
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
        binding.textError.text = state.error.orEmpty()
        binding.textError.visibility = if (state.error == null) View.GONE else View.VISIBLE

        binding.buttonSave.isEnabled = !state.isSaving
        binding.buttonSave.text = getString(if (state.isSaving) R.string.saving else R.string.save)

        if (state.isSaved) {
            setResult(RESULT_OK)
            finish()
        }
    }
}
