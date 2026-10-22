package com.kelvinht.moneyapp.feature.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.MainActivity
import com.kelvinht.moneyapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        viewModel = ViewModelProvider(this, HomeViewModelFactory())[HomeViewModel::class.java]
        render(viewModel.loadHome())
        setupNavigation()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: HomeUiState) = with(binding) {
        textDate.text = state.dateLabel
        textSpent.text = state.spent
        textUsage.text = state.usageLabel
        textLimit.text = "Batas harian: " + state.limit
        textRemaining.text = "Sisa " + state.remaining
        textAverage.text = state.averageDaily
        textCondition.text = state.budgetCondition
        progressDaily.progress = state.progress

        val names = listOf(textCategoryOne, textCategoryTwo, textCategoryThree)
        val amounts = listOf(textCategoryAmountOne, textCategoryAmountTwo, textCategoryAmountThree)
        val progressBars = listOf(progressCategoryOne, progressCategoryTwo, progressCategoryThree)
        state.categories.take(3).forEachIndexed { index, category ->
            names[index].text = category.name
            amounts[index].text = category.amountLabel
            progressBars[index].progress = category.progress
        }
        emptyState.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
    }

    private fun setupNavigation() = with(binding) {
        buttonAddExpense.setOnClickListener { (activity as? MainActivity)?.openTransactionEntry() }
        navStatistics.setOnClickListener { (activity as? MainActivity)?.openReports() }
        navBudget.setOnClickListener { (activity as? MainActivity)?.openWallets() }
    }
}
