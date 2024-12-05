package com.kelvinht.moneyapp.adapter.onclick

import com.kelvinht.moneyapp.data.MoneyIn

interface OnClickMoneyInItem {
    fun editMoneyIn(moneyIn: MoneyIn)
    fun deleteMoneyIn(moneyIn: MoneyIn)
}