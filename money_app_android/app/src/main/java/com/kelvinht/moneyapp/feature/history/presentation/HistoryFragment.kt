package com.kelvinht.moneyapp.feature.history.presentation

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
import com.kelvinht.moneyapp.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, HistoryViewModelFactory())[HistoryViewModel::class.java]
        render(viewModel.loadHistory())
        setupKeypad()
        setupSourceChips()
        setupNavigation()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupKeypad() {
        mapOf(
            binding.key1 to "1",
            binding.key2 to "2",
            binding.key3 to "3",
            binding.key4 to "4",
            binding.key5 to "5",
            binding.key6 to "6",
            binding.key7 to "7",
            binding.key8 to "8",
            binding.key9 to "9",
            binding.key00 to "00",
            binding.key0 to "0"
        ).forEach { (view, key) ->
            view.setOnClickListener { render(viewModel.onKeyPressed(key)) }
        }
        binding.keyBackspace.setOnClickListener { render(viewModel.onKeyPressed("backspace")) }
    }

    private fun setupSourceChips() {
        binding.chipCash.setOnClickListener { render(viewModel.selectSource("Tunai")) }
        binding.chipOvo.setOnClickListener { render(viewModel.selectSource("OVO")) }
        binding.chipGopay.setOnClickListener { render(viewModel.selectSource("GoPay")) }
    }

    private fun render(state: HistoryUiState) {
        binding.textAmount.text = state.amount
        binding.textCategory.text = state.category
        binding.textDate.text = state.datePlaceholder
        bindChip(binding.chipCash, state.sources.getOrNull(0))
        bindChip(binding.chipOvo, state.sources.getOrNull(1))
        bindChip(binding.chipGopay, state.sources.getOrNull(2))
        binding.buttonSave.alpha = if (state.isSaveEnabled) 1f else 0.75f
    }

    private fun bindChip(view: TextView, source: FundingSourceUi?) {
        if (source == null) return
        view.text = source.name
        view.setBackgroundResource(if (source.isSelected) R.drawable.bg_source_selected else R.drawable.bg_source_unselected)
        view.setTextColor(color(if (source.isSelected) R.color.white else R.color.home_text))
    }

    private fun setupNavigation() {
        binding.buttonBack.setOnClickListener { (activity as? MainActivity)?.openHome() }
        binding.navHome.root.setOnClickListener { (activity as? MainActivity)?.openHome() }
        binding.navWallets.root.setOnClickListener { (activity as? MainActivity)?.openWallets() }
        binding.navReports.root.setOnClickListener { (activity as? MainActivity)?.openReports() }
    }

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)
}
