package com.kelvinht.moneyapp.feature.wallets.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.MainActivity
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.databinding.FragmentWalletsBinding
import com.kelvinht.moneyapp.databinding.ItemWalletAccountBinding

class WalletsFragment : Fragment() {
    private var _binding: FragmentWalletsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: WalletsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWalletsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, WalletsViewModelFactory())[WalletsViewModel::class.java]
        render(viewModel.loadWallets())
        setupNavigation()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun render(state: WalletsUiState) {
        binding.textUserInitials.text = state.userInitials
        val rows = listOf(binding.accountFirst, binding.accountSecond, binding.accountThird, binding.accountFourth)
        rows.forEachIndexed { index, row ->
            state.accounts.getOrNull(index)?.let { bindAccount(row, it) } ?: run { row.root.visibility = View.GONE }
        }
    }

    private fun bindAccount(binding: ItemWalletAccountBinding, account: WalletAccountUi) {
        binding.textAccountName.text = account.name
        binding.textAccountDescription.text = account.description
        binding.textAccountBalance.text = account.balance
        when (account.style) {
            WalletAccountStyle.CASH -> applyAccountStyle(binding, R.color.home_card_green, R.drawable.bg_home_icon_green, R.drawable.ic_wallet_cash_colored)
            WalletAccountStyle.OVO -> applyAccountStyle(binding, R.color.wallet_ovo, R.drawable.bg_home_icon_blue, R.drawable.ic_home_wallet)
            WalletAccountStyle.GOPAY -> applyAccountStyle(binding, R.color.wallet_cyan, R.drawable.bg_home_icon_blue, R.drawable.ic_home_wallet)
            WalletAccountStyle.DANA -> applyAccountStyle(binding, R.color.home_primary, R.drawable.bg_home_icon_blue, R.drawable.ic_home_wallet)
        }
    }

    private fun applyAccountStyle(binding: ItemWalletAccountBinding, accentColor: Int, iconBackground: Int, icon: Int) {
        binding.viewAccent.setBackgroundColor(color(accentColor))
        binding.imageAccountIcon.setBackgroundResource(iconBackground)
        binding.imageAccountIcon.setImageResource(icon)
    }

    private fun setupNavigation() {
        binding.navHome.root.setOnClickListener { (activity as? MainActivity)?.openHome() }
        binding.navHistory.root.setOnClickListener { (activity as? MainActivity)?.openHistory() }
        binding.navReports.root.setOnClickListener { (activity as? MainActivity)?.openReports() }
    }

    private fun color(resId: Int): Int = ContextCompat.getColor(requireContext(), resId)
}
