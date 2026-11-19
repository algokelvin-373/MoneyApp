package com.kelvinht.moneylife.presentation.home

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.databinding.ItemExpenseBinding

class ExpenseAdapter(
    private val onDeleteClick: (ExpenseUiModel) -> Unit,
) : ListAdapter<ExpenseUiModel, ExpenseAdapter.ExpenseViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ExpenseViewHolder {
        val binding =
            ItemExpenseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ExpenseViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(
        holder: ExpenseViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    class ExpenseViewHolder(
        private val binding: ItemExpenseBinding,
        private val onDeleteClick: (ExpenseUiModel) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseUiModel) {
            val context = binding.root.context

            binding.textEmoji.text = item.style.emoji
            binding.textEmoji.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, item.style.colorRes))
            binding.textCategory.text = context.getString(item.style.labelRes)
            binding.textAmount.text = RupiahFormatter.format(item.amount)
            binding.textTime.text = DateUtils.formatTime(item.createdAt)

            if (item.note.isBlank()) {
                binding.textNote.visibility = View.GONE
            } else {
                binding.textNote.visibility = View.VISIBLE
                binding.textNote.text = item.note
            }

            binding.buttonDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    private companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ExpenseUiModel>() {
                override fun areItemsTheSame(
                    oldItem: ExpenseUiModel,
                    newItem: ExpenseUiModel,
                ): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ExpenseUiModel,
                    newItem: ExpenseUiModel,
                ): Boolean = oldItem == newItem
            }
    }
}
