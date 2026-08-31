package com.kelvinht.moneyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelvinht.moneyapp.base.list_money_in.ListMoneyInFragment
import com.kelvinht.moneyapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, ListMoneyInFragment())
                .commit()
        }
    }
}