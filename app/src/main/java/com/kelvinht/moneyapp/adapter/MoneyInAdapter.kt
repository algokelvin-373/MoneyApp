package com.kelvinht.moneyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneyapp.data.Transaction
import com.kelvinht.moneyapp.databinding.ItemMoneyInBinding


class MoneyInAdapter() : RecyclerView.Adapter<MoneyInAdapter.TransactionViewHolder>() {
    private lateinit var binding: ItemMoneyInBinding
    private var listMoneyIn = ArrayList<Transaction>()

    fun setList(list: ArrayList<Transaction>) {
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

    /*fun setTransactions(list: ArrayList<Transaction>) {
        listMoneyIn = list
        notifyDataSetChanged()
    }*/

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(transaction: Transaction) {
            val dataFrom = transaction.dataFrom
            val dataTo = transaction.dataInto
            val dataCombine = "Dari $dataFrom ke $dataTo"

            binding.txtDataFromTo.text = dataCombine
            binding.txtTime.text = transaction.time
            binding.txtDescription.text = transaction.description
            binding.txtAmount.text = transaction.amount.toString()
        }
    }
}
