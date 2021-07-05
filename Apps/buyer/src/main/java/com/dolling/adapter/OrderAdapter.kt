package com.dolling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolling.databinding.ItemOrderBinding
import com.dolling.modal.MenuLocalModal

class OrderAdapter() : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val list = arrayListOf<MenuLocalModal>()

    fun setList(list: List<MenuLocalModal>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun delList() {
        this.list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setViewValue(menu: MenuLocalModal) {
            binding.apply {
                tvFoodName.text = menu.name

                tvOrderAmount.text = "${menu.quantity}x"

                val totalPrices = menu.price * menu.quantity

                tvTotalPricesItem.text = "Rp. $totalPrices"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setViewValue(list[position])
    }

    override fun getItemCount(): Int = list.size
}