package com.example.handsight

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.handsight.databinding.ItemHistoryBinding

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private lateinit var binding: ItemHistoryBinding

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HistoryAdapter.HistoryViewHolder {
        binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder()
    }

    override fun onBindViewHolder(holder: HistoryAdapter.HistoryViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class HistoryViewHolder : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: History) {
            binding.tvItemName.text = data.name
            binding.tvItemDate.text = data.createdAt
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<History>() {
        override fun areItemsTheSame(oldItem: History, newItem: History): Boolean =
            oldItem.createdAt == newItem.createdAt

        override fun areContentsTheSame(oldItem: History, newItem: History): Boolean =
            oldItem == newItem
    }

    val differ = AsyncListDiffer(this, differCallback)
}