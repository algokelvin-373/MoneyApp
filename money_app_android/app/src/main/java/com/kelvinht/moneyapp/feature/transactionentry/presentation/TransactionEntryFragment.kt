package com.kelvinht.moneyapp.feature.transactionentry.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.MainActivity
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.databinding.FragmentTransactionEntryBinding

class TransactionEntryFragment : Fragment() {
    private var _binding: FragmentTransactionEntryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TransactionEntryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentTransactionEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        viewModel = ViewModelProvider(this, TransactionEntryViewModelFactory())[TransactionEntryViewModel::class.java]
        render(viewModel.load())
        setupActions()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupActions() = with(binding) {
        buttonBack.setOnClickListener { (activity as? MainActivity)?.openHome() }
        buttonReset.setOnClickListener { render(viewModel.reset()) }
        quick10.setOnClickListener { render(viewModel.addQuickAmount(10_000)) }
        quick20.setOnClickListener { render(viewModel.addQuickAmount(20_000)) }
        quick50.setOnClickListener { render(viewModel.addQuickAmount(50_000)) }
        quick100.setOnClickListener { render(viewModel.addQuickAmount(100_000)) }
        categoryFood.setOnClickListener { render(viewModel.selectCategory("Makan")) }
        categoryTransport.setOnClickListener { render(viewModel.selectCategory("Transport")) }
        categorySnack.setOnClickListener { render(viewModel.selectCategory("Jajan")) }
    }

    private fun render(state: TransactionEntryUiState) = with(binding) {
        textAmount.text = state.amount
        textDate.text = state.transactionDate
        textSource.text = state.source
        textNote.text = state.note
        val cards = listOf(categoryFood, categoryTransport, categorySnack)
        val checks = listOf(checkFood, checkTransport, checkSnack)
        state.categories.take(3).forEachIndexed { index, category ->
            cards[index].setBackgroundResource(
                if (category.isSelected) R.drawable.bg_ml_category_selected
                else R.drawable.bg_ml_category
            )
            checks[index].visibility = if (category.isSelected) View.VISIBLE else View.GONE
        }
        buttonSave.alpha = if (state.isSaveEnabled) 1f else 0.55f
        buttonSave.isEnabled = state.isSaveEnabled
    }
}
