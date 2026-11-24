package com.kelvinht.moneylife.presentation.home

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.databinding.ItemCategorySummaryBinding
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.presentation.common.toStyle

data class CategorySummaryUiModel(
    val category: ExpenseCategory,
    val spent: Long,
)

class CategorySummaryAdapter :
    ListAdapter<CategorySummaryUiModel, CategorySummaryAdapter.ViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder =
        ViewHolder(
            ItemCategorySummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) = holder.bind(getItem(position))

    class ViewHolder(
        private val binding: ItemCategorySummaryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategorySummaryUiModel) {
            val context = binding.root.context
            val style = item.category.toStyle()
            binding.textCategoryIcon.text = style.emoji
            binding.textCategoryIcon.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, style.colorRes))
            binding.textCategoryName.setText(style.labelRes)
            binding.textCategoryAmount.text =
                context.getString(
                    R.string.category_spent_format,
                    RupiahFormatter.format(item.spent),
                )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CategorySummaryUiModel>() {
        override fun areItemsTheSame(
            oldItem: CategorySummaryUiModel,
            newItem: CategorySummaryUiModel,
        ): Boolean = oldItem.category == newItem.category

        override fun areContentsTheSame(
            oldItem: CategorySummaryUiModel,
            newItem: CategorySummaryUiModel,
        ): Boolean = oldItem == newItem
    }
}
