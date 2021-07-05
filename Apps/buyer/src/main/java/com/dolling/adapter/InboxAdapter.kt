package com.dolling.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dolling.databinding.ItemInboxBinding
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.modal.InboxModal
import com.dolling.utils.Utils.countDaysTimestamp
import java.util.Date

class InboxAdapter(
    private val adapterOnClickListener: AdapterOnClickListener
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    private val list = arrayListOf<InboxModal>()

    fun setList(list: List<InboxModal>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun delList() {
        this.list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        private val binding: ItemInboxBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setViewValue(inbox: InboxModal) {
            binding.tvInboxTitle.text = inbox.title
            binding.tvInboxMessage.text = inbox.message
            val time = countDaysTimestamp(inbox.timestamp.toDate())
            binding.tvInboxTime.text = time
        }

        fun setOnClickListener(inbox: InboxModal) {
            binding.container.setOnClickListener {
                adapterOnClickListener.onAdapterParentViewClicked(inbox)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemInboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val inbox = list[position]
        holder.setViewValue(inbox)
        holder.setOnClickListener(inbox)
    }

    override fun getItemCount(): Int = list.size
}