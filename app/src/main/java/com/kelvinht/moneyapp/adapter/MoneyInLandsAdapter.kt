package com.kelvinht.moneyapp.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.data.MoneyInTitle
import com.kelvinht.moneyapp.data.MoneyInTotal
import com.kelvinht.moneyapp.databinding.ItemMoneyInBinding
import com.kelvinht.moneyapp.databinding.ItemMoneyInTitleBinding
import com.kelvinht.moneyapp.databinding.ItemMoneyInTotalBinding


class MoneyInLandsAdapter(
    private val bounds: Rect,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listMoneyIn = ArrayList<Any>()

    fun setList(list: ArrayList<Any>) {
        listMoneyIn.clear()
        listMoneyIn.addAll(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewTypeMoneyIn.TITLE.ordinal -> MoneyInTitleViewHolder(
                ItemMoneyInTitleBinding.inflate(inflater, parent, false)
            )
            ViewTypeMoneyIn.MONEY_IN.ordinal -> MoneyInViewHolder(
                ItemMoneyInBinding.inflate(inflater, parent, false)
            )
            else -> MoneyInTotalViewHolder(
                ItemMoneyInTotalBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (listMoneyIn[position]) {
            is MoneyInTitle -> ViewTypeMoneyIn.TITLE.ordinal
            is MoneyIn -> ViewTypeMoneyIn.MONEY_IN.ordinal
            else -> ViewTypeMoneyIn.TOTAL.ordinal
        }
    }

    override fun getItemCount(): Int = listMoneyIn.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listMoneyIn[position]) {
            is MoneyInTitle -> (holder as MoneyInTitleViewHolder).bind(item)
            is MoneyIn -> (holder as MoneyInViewHolder).bind(item)
            is MoneyInTotal -> (holder as MoneyInTotalViewHolder).bind(item)
        }
    }

    inner class MoneyInTitleViewHolder(private val binding: ItemMoneyInTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MoneyInTitle) {
            binding.txtTime?.text = data.date
        }
    }

    inner class MoneyInViewHolder(private val binding: ItemMoneyInBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(moneyIn: MoneyIn) {
            val dataFrom = moneyIn.dataFrom
            val dataTo = moneyIn.dataInto
            val dataCombine = "Dari $dataFrom ke $dataTo"

            binding.txtTime.text = moneyIn.time
            binding.txtDataFrom?.text = moneyIn.dataFrom
            binding.txtDataTo?.text = moneyIn.dataInto
            binding.txtDataFromTo?.text = dataCombine
            binding.txtDescription.text = moneyIn.description
            binding.txtAmount.text = moneyIn.amount.toString()
        }
    }

    inner class MoneyInTotalViewHolder(private val binding: ItemMoneyInTotalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MoneyInTotal) {
            binding.txtDescription?.text = "Total"
            binding.txtAmount.text = data.totalAmount.toString()
            binding.txtTime.text = data.date

            if (bounds.width() > bounds.height()) {
                binding.txtTime.visibility = View.INVISIBLE
            } else {
                binding.txtTime.visibility = View.VISIBLE
            }
        }
    }
}