package com.dolling.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolling.R
import com.dolling.databinding.ItemFoodBinding
import com.dolling.glide.GlideApp
import com.dolling.modal.MenuLocalModal
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MenuLocalAdapter(
    private val menuLocalAdapterListener: MenuLocalAdapterListener,
    private val sellerId: String
) : RecyclerView.Adapter<MenuLocalAdapter.ViewHolder>() {

    private val storageRef = Firebase.storage.getReference("menu/$sellerId")
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
        private val context: Context,
        private val binding: ItemFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setViewValue(menu: MenuLocalModal) {
            binding.apply {
                GlideApp.with(context)
                    .load(storageRef.child(menu.photo_url))
                    .placeholder(R.drawable.food_image_placeholder)
                    .into(ivFoodImage)

                tvFoodName.text = menu.name

                tvFoodDescription.text = menu.description

                tvFoodPrice.text = "Rp. ${menu.price}"

                if (menu.quantity != 0)
                    tvQuantity.text = "${menu.quantity}x"
                else tvQuantity.text = ""
            }
        }

        fun setOnClickListener(position: Int) {
            binding.container.setOnClickListener {
                menuLocalAdapterListener.onClickListener(position)
            }
            binding.container.setOnLongClickListener {
                menuLocalAdapterListener.onLongClickListener(position)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            parent.context,
            ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = list[position]
        holder.setViewValue(menu)
        holder.setOnClickListener(position)
    }

    override fun getItemCount(): Int = list.size

    interface MenuLocalAdapterListener {
        fun onClickListener(position: Int)

        fun onLongClickListener(position: Int)
    }
}