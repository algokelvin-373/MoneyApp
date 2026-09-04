package com.kelvinht.moneyapp.feature.transactionentry.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
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
        setupKeypad()
        binding.chipCash.setOnClickListener { render(viewModel.selectSource("Tunai")) }
        binding.chipOvo.setOnClickListener { render(viewModel.selectSource("OVO")) }
        binding.chipGopay.setOnClickListener { render(viewModel.selectSource("GoPay")) }
        binding.buttonBack.setOnClickListener { (activity as? MainActivity)?.openHome() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupKeypad() {
        mapOf(
            binding.key1 to "1", binding.key2 to "2", binding.key3 to "3",
            binding.key4 to "4", binding.key5 to "5", binding.key6 to "6",
            binding.key7 to "7", binding.key8 to "8", binding.key9 to "9",
            binding.key00 to "00", binding.key0 to "0"
        ).forEach { (keyView, key) -> keyView.setOnClickListener { render(viewModel.onKeyPressed(key)) } }
        binding.keyBackspace.setOnClickListener { render(viewModel.onKeyPressed("backspace")) }
    }

    private fun render(state: TransactionEntryUiState) {
        binding.textAmount.text = state.amount
        binding.textCategory.text = state.category
        binding.textDate.text = state.datePlaceholder
        bindChip(binding.chipCash, state.sources.getOrNull(0))
        bindChip(binding.chipOvo, state.sources.getOrNull(1))
        bindChip(binding.chipGopay, state.sources.getOrNull(2))
        binding.buttonSave.alpha = if (state.isSaveEnabled) 1f else 0.65f
        binding.buttonSave.isEnabled = state.isSaveEnabled
    }

    private fun bindChip(view: TextView, source: FundingSourceUi?) {
        source ?: return
        view.text = source.name
        view.setBackgroundResource(if (source.isSelected) R.drawable.bg_source_selected else R.drawable.bg_source_unselected)
        view.setTextColor(ContextCompat.getColor(requireContext(), if (source.isSelected) R.color.white else R.color.home_text))
    }
}
