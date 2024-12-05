package com.kelvinht.moneyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.databinding.ItemMoneyInBinding


class MoneyInAdapter() : RecyclerView.Adapter<MoneyInAdapter.TransactionViewHolder>() {
    private lateinit var binding: ItemMoneyInBinding
    private var listMoneyIn = ArrayList<MoneyIn>()

    fun setList(list: ArrayList<MoneyIn>) {
        listMoneyIn.clear()
        listMoneyIn.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        binding = ItemMoneyInBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(listMoneyIn[position])
    }

    override fun getItemCount(): Int {
        return listMoneyIn.size
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(moneyIn: MoneyIn) {
            val dataFrom = moneyIn.dataFrom
            val dataTo = moneyIn.dataInto
            val dataCombine = "Dari $dataFrom ke $dataTo"

            binding.txtDataFromTo?.text = dataCombine
            binding.txtTime.text = moneyIn.time
            binding.txtDescription.text = moneyIn.description
            binding.txtAmount.text = moneyIn.amount.toString()
        }
    }
}
