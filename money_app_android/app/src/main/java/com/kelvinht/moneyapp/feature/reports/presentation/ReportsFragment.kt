package com.kelvinht.moneyapp.feature.reports.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.MainActivity
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.databinding.FragmentReportsBinding
import com.kelvinht.moneyapp.databinding.ItemReportCategoryBinding

class ReportsFragment : Fragment() {
    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ReportsViewModelFactory())[ReportsViewModel::class.java]
        render(viewModel.loadReports())
        setupNavigation()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: ReportsUiState) {
        binding.textTotalExpense.text = state.totalExpenseCompact
        binding.donutChart.setSegments(state.categories.map { it.percentage })
        binding.textRemainingBudget.text = state.remainingBudget
        binding.textTotalBudget.text = state.totalBudget
        binding.progressBudget.progress = state.budgetProgress
        binding.textUsedBudget.text = state.usedBudget
        binding.textBudgetStatus.text = state.budgetStatus
        binding.textTipTitle.text = state.weeklyTipTitle
        binding.textTipDescription.text = state.weeklyTipDescription

        val rows = listOf(binding.categoryFirst, binding.categorySecond, binding.categoryThird)
        rows.forEachIndexed { index, row ->
            state.categories.getOrNull(index)?.let { bindCategory(row, it) } ?: run { row.root.visibility = View.GONE }
        }
    }

    private fun bindCategory(binding: ItemReportCategoryBinding, category: ExpenseCategoryUi) {
        binding.textCategoryName.text = category.name
        binding.textCategoryPercentage.text = category.percentageLabel
        val dot = when (category.style) {
            ExpenseCategoryUiStyle.FOOD -> R.drawable.bg_category_blue_dot
            ExpenseCategoryUiStyle.TRANSPORT -> R.drawable.bg_category_purple_dot
            ExpenseCategoryUiStyle.SAVING -> R.drawable.bg_category_green_dot
        }
        binding.viewCategoryDot.setBackgroundResource(dot)
    }

    private fun setupNavigation() {
        binding.navHome.root.setOnClickListener { (activity as? MainActivity)?.openHome() }
        binding.navWallets.root.setOnClickListener { (activity as? MainActivity)?.openWallets() }
        binding.navHistory.root.setOnClickListener { (activity as? MainActivity)?.openHistory() }
    }
}
