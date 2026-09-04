package com.kelvinht.moneyapp.feature.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.MainActivity
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.databinding.FragmentHomeBinding
import com.kelvinht.moneyapp.databinding.ItemHomeTransactionBinding
import com.kelvinht.moneyapp.databinding.ItemHomeWalletBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, HomeViewModelFactory())[HomeViewModel::class.java]
        render(viewModel.loadHome())
        setupNavigation()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: HomeUiState) {
        binding.textGreeting.text = state.greeting
        binding.textTotalBalance.text = state.totalBalance
        binding.textMonthlyGrowth.text = state.monthlyGrowth
        bindWallets(state.wallets)
        bindTransactions(state.transactions)
    }

    private fun bindWallets(wallets: List<HomeWalletUi>) {
        val bindings = listOf(binding.walletCash, binding.walletEwallet, binding.walletBank)
        bindings.forEachIndexed { index, itemBinding ->
            wallets.getOrNull(index)?.let { wallet ->
                bindWallet(itemBinding, wallet)
            } ?: run {
                itemBinding.root.visibility = View.GONE
            }
        }
    }

    private fun bindWallet(binding: ItemHomeWalletBinding, wallet: HomeWalletUi) {
        binding.textWalletName.text = wallet.name
        binding.textWalletBalance.text = wallet.balance
        when (wallet.style) {
            WalletStyle.CASH -> {
                binding.root.setBackgroundResource(R.drawable.bg_home_wallet_light)
                binding.imageWalletIcon.setImageResource(R.drawable.ic_home_cash)
                binding.textWalletBalance.setTextColor(color(R.color.home_text))
            }
            WalletStyle.E_WALLET -> {
                binding.root.setBackgroundResource(R.drawable.bg_home_wallet_green)
                binding.imageWalletIcon.setImageResource(R.drawable.ic_home_wallet)
                binding.textWalletBalance.setTextColor(color(R.color.home_green))
            }
            WalletStyle.BANK -> {
                binding.root.setBackgroundResource(R.drawable.bg_home_wallet_light)
                binding.imageWalletIcon.setImageResource(R.drawable.ic_nav_reports)
                binding.textWalletBalance.setTextColor(color(R.color.home_text))
            }
        }
    }

    private fun bindTransactions(transactions: List<HomeTransactionUi>) {
        val bindings = listOf(binding.transactionFirst, binding.transactionSecond, binding.transactionThird)
        bindings.forEachIndexed { index, itemBinding ->
            transactions.getOrNull(index)?.let { transaction ->
                bindTransaction(itemBinding, transaction)
            } ?: run {
                itemBinding.root.visibility = View.GONE
            }
        }
    }

    private fun bindTransaction(binding: ItemHomeTransactionBinding, transaction: HomeTransactionUi) {
        binding.textTransactionTitle.text = transaction.title
        binding.textTransactionSubtitle.text = transaction.subtitle
        binding.textTransactionAmount.text = transaction.amount
        when (transaction.style) {
            TransactionStyle.EXPENSE -> {
                binding.imageTransactionIcon.setBackgroundResource(R.drawable.bg_home_icon_red)
                binding.imageTransactionIcon.setImageResource(R.drawable.ic_transaction_food)
                binding.textTransactionAmount.setTextColor(color(R.color.home_red))
            }
            TransactionStyle.TRANSFER -> {
                binding.imageTransactionIcon.setBackgroundResource(R.drawable.bg_home_icon_blue)
                binding.imageTransactionIcon.setImageResource(R.drawable.ic_transaction_transfer)
                binding.textTransactionAmount.setTextColor(color(R.color.home_text))
            }
            TransactionStyle.INCOME -> {
                binding.imageTransactionIcon.setBackgroundResource(R.drawable.bg_home_icon_green)
                binding.imageTransactionIcon.setImageResource(R.drawable.ic_transaction_income)
                binding.textTransactionAmount.setTextColor(color(R.color.home_green))
            }
        }
    }

    private fun color(resId: Int): Int {
        return ContextCompat.getColor(requireContext(), resId)
    }

    private fun setupNavigation() {
        binding.buttonAddTransaction.setOnClickListener { (activity as? MainActivity)?.openTransactionEntry() }
        binding.textSeeAll.setOnClickListener { (activity as? MainActivity)?.openHistory() }
        binding.navWallets.root.setOnClickListener { (activity as? MainActivity)?.openWallets() }
        binding.navHistory.root.setOnClickListener { (activity as? MainActivity)?.openHistory() }
        binding.navReports.root.setOnClickListener { (activity as? MainActivity)?.openReports() }
    }
}
