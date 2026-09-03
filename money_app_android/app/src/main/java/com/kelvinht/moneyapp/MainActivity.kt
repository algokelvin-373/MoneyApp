package com.kelvinht.moneyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kelvinht.moneyapp.databinding.ActivityMainBinding
import com.kelvinht.moneyapp.feature.history.presentation.HistoryFragment
import com.kelvinht.moneyapp.feature.home.presentation.HomeFragment
import com.kelvinht.moneyapp.feature.reports.presentation.ReportsFragment
import com.kelvinht.moneyapp.feature.wallets.presentation.WalletsFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            openHome()
        }
    }

    fun openHome() = showFragment(HomeFragment())

    fun openWallets() = showFragment(WalletsFragment())

    fun openHistory() = showFragment(HistoryFragment())

    fun openReports() = showFragment(ReportsFragment())

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
