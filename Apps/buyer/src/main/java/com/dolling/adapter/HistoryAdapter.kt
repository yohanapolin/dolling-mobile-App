package com.dolling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolling.databinding.ItemHistoryInProgressFoodBinding
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.modal.OrderModal
import com.dolling.utils.Utils

class HistoryAdapter(
    private val adapterOnClickListener: AdapterOnClickListener
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val list = arrayListOf<OrderModal>()

    fun setList(list: List<OrderModal>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun delList() {
        this.list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemHistoryInProgressFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setViewValue(order: OrderModal) {
            binding.apply {
                tvStoreName.text = order.store_name
                tvItemCount.text = "${order.item_count} items"
                tvPaymentMethod.text = "(${order.payment_method})"
                tvTotalPrices.text = "Rp. ${order.total_price}"
                val days = Utils.countDaysTimestamp(order.created_at.toDate())
                binding.tvTimestamp.text = days
            }
        }

        fun setViewOnClickListener(order: OrderModal) {
            binding.container.setOnClickListener {
                adapterOnClickListener.onAdapterParentViewClicked(order)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHistoryInProgressFoodBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = list[position]
        holder.setViewValue(order)
        holder.setViewOnClickListener(order)
    }

    override fun getItemCount(): Int = list.size
}