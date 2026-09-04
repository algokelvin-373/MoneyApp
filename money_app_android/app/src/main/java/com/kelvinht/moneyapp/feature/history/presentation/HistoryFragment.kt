package com.kelvinht.moneyapp.feature.history.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.MainActivity
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.databinding.FragmentHistoryBinding
import com.kelvinht.moneyapp.databinding.ItemHistoryGroupBinding
import com.kelvinht.moneyapp.databinding.ItemHistoryTransactionBinding
import com.kelvinht.moneyapp.feature.history.domain.model.HistoryTransactionType

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, state: Bundle?) {
        super.onViewCreated(view, state)
        viewModel = ViewModelProvider(this, HistoryViewModelFactory())[HistoryViewModel::class.java]
        render(viewModel.loadHistory())
        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(value: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(value: CharSequence?, start: Int, before: Int, count: Int) {
                render(viewModel.loadHistory(value?.toString().orEmpty()))
            }
            override fun afterTextChanged(value: Editable?) = Unit
        })
        binding.navHome.root.setOnClickListener { (activity as? MainActivity)?.openHome() }
        binding.navWallets.root.setOnClickListener { (activity as? MainActivity)?.openWallets() }
        binding.navReports.root.setOnClickListener { (activity as? MainActivity)?.openReports() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: HistoryUiState) {
        binding.textMonth.text = state.monthLabel
        binding.textTotalIncome.text = state.totalIncome
        binding.textIncomeCount.text = state.incomeCount
        binding.textTotalExpense.text = state.totalExpense
        binding.textExpenseCount.text = state.expenseCount
        binding.textNetCashFlow.text = state.netCashFlow
        binding.textNetCashFlow.setTextColor(color(if (state.isSurplus) R.color.home_green else R.color.home_red))
        binding.progressIncome.layoutParams = (binding.progressIncome.layoutParams as LinearLayout.LayoutParams).apply {
            weight = state.incomeRatio
        }
        binding.progressExpense.layoutParams = (binding.progressExpense.layoutParams as LinearLayout.LayoutParams).apply {
            weight = 1f - state.incomeRatio
        }
        binding.textDisplayInfo.text = state.displayInfo
        binding.buttonLoadPrevious.visibility = if (state.hasResults) View.VISIBLE else View.GONE
        renderGroups(state.groups)
    }

    private fun renderGroups(groups: List<HistoryGroupUi>) {
        binding.historyGroupsContainer.removeAllViews()
        groups.forEach { group ->
            val groupBinding = ItemHistoryGroupBinding.inflate(layoutInflater, binding.historyGroupsContainer, false)
            groupBinding.textGroupDate.text = group.dateLabel
            groupBinding.textGroupTotal.text = group.dailyTotal
            groupBinding.textGroupTotal.setTextColor(color(if (group.dailyTotal.startsWith("+")) R.color.home_green else R.color.home_muted))
            group.transactions.forEachIndexed { index, transaction ->
                if (index > 0) groupBinding.transactionsContainer.addView(divider())
                groupBinding.transactionsContainer.addView(createTransactionView(transaction))
            }
            binding.historyGroupsContainer.addView(groupBinding.root)
        }
    }

    private fun createTransactionView(item: HistoryTransactionUi): View {
        val itemBinding = ItemHistoryTransactionBinding.inflate(layoutInflater, binding.historyGroupsContainer, false)
        itemBinding.textTitle.text = item.title
        itemBinding.textCategory.text = item.category
        itemBinding.textAccountTime.text = item.accountAndTime
        itemBinding.textAmount.text = item.amount
        itemBinding.textAmount.setTextColor(color(if (item.isIncome) R.color.home_green else R.color.home_text))
        val (icon, background) = when (item.type) {
            HistoryTransactionType.FOOD -> R.drawable.ic_transaction_food to R.drawable.bg_home_icon_red
            HistoryTransactionType.INCOME -> R.drawable.ic_wallet_cash_colored to R.drawable.bg_home_icon_green
            HistoryTransactionType.CASHBACK -> R.drawable.ic_home_cash to R.drawable.bg_home_icon_green
            HistoryTransactionType.TRANSFER -> R.drawable.ic_transaction_transfer to R.drawable.bg_home_icon_blue
            HistoryTransactionType.SHOPPING -> R.drawable.ic_nav_reports to R.drawable.bg_home_icon_blue
            HistoryTransactionType.TRANSPORT -> R.drawable.ic_nav_history to R.drawable.bg_home_icon_blue
        }
        itemBinding.imageIcon.setImageResource(icon)
        itemBinding.imageIcon.setBackgroundResource(background)
        if (item.type == HistoryTransactionType.INCOME) itemBinding.root.setBackgroundResource(R.drawable.bg_history_income_row)
        return itemBinding.root
    }

    private fun divider() = View(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1.dp)
        setBackgroundResource(R.drawable.bg_history_divider)
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private fun color(resource: Int) = ContextCompat.getColor(requireContext(), resource)
}
