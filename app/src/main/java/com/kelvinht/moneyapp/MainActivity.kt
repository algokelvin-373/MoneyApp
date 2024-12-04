package com.kelvinht.moneyapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kelvinht.moneyapp.base.transaction.TransactionFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TransactionFragment())
                .commit()
        }
    }
}