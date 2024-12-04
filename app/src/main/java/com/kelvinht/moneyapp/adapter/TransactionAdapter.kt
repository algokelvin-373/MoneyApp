package com.kelvinht.moneyapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.data.Transaction


class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {
    private var transactions: List<Transaction> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction: Transaction = transactions[position]
//        holder.titleTextView.setText(currentTransaction.title)
//        holder.amountTextView.text = "Rp " + currentTransaction.amount
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    fun setTransactions(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var titleTextView: TextView = itemView.findViewById<TextView>(R.id.textViewTitle)
//        var amountTextView: TextView = itemView.findViewById<TextView>(R.id.textViewAmount)

    }
}
