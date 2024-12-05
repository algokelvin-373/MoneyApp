package com.kelvinht.moneyapp.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.adapter.onclick.OnClickMoneyInItem
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.data.MoneyInTitle
import com.kelvinht.moneyapp.data.MoneyInTotal
import com.kelvinht.moneyapp.databinding.ItemMoneyInBinding
import com.kelvinht.moneyapp.databinding.ItemMoneyInTitleBinding
import com.kelvinht.moneyapp.databinding.ItemMoneyInTotalBinding
import com.kelvinht.moneyapp.utils.GlobalFunction


class MoneyInLandsAdapter(
    private val bounds: Rect,
    private val listMoneyIn: ArrayList<Any>,
    private val onClickMoneyInItem: OnClickMoneyInItem,
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
        val context = holder.itemView.context
        when (val item = listMoneyIn[position]) {
            is MoneyInTitle -> (holder as MoneyInTitleViewHolder).bind(item)
            is MoneyIn -> (holder as MoneyInViewHolder).bind(item, context)
            is MoneyInTotal -> (holder as MoneyInTotalViewHolder).bind(item, context)
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
        fun bind(moneyIn: MoneyIn, context: Context) {
            if (moneyIn.amount == -1) {
                binding.txtTime.text = moneyIn.time
                binding.txtDataFrom?.text = moneyIn.dataFrom
                binding.txtDataTo?.text = moneyIn.dataInto
                binding.txtDescription.text = moneyIn.description
                binding.txtAmount.text = context.getString(R.string.title_amount)
            } else {
                val dataFrom = moneyIn.dataFrom
                val dataTo = moneyIn.dataInto
                val dataCombine = "Dari $dataFrom ke $dataTo"
                binding.txtTime.text = moneyIn.time
                binding.txtDataFrom?.text = moneyIn.dataFrom
                binding.txtDataTo?.text = moneyIn.dataInto
                binding.txtDataFromTo?.text = dataCombine
                binding.txtDescription.text = moneyIn.description
                binding.txtAmount.text = GlobalFunction.formatRupiah(moneyIn.amount)
            }
            binding.layoutEdit?.setOnClickListener {
                onClickMoneyInItem.editMoneyIn(moneyIn)
            }
            binding.layoutDelete?.setOnClickListener {
                onClickMoneyInItem.deleteMoneyIn(moneyIn)
            }
        }
    }

    inner class MoneyInTotalViewHolder(private val binding: ItemMoneyInTotalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: MoneyInTotal, context: Context) {
            binding.txtDescription?.text = context.getString(R.string.title_total)
            binding.txtAmount.text = GlobalFunction.formatRupiah(data.totalAmount)
            binding.txtTime.text = data.date

            if (bounds.width() > bounds.height()) {
                binding.txtTime.visibility = View.INVISIBLE
            } else {
                binding.txtTime.visibility = View.VISIBLE
            }
        }
    }
}