package com.dolling.ui.main.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolling.adapter.HistoryAdapter
import com.dolling.databinding.FragmentHistoryInProgressBinding
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.modal.OrderModal
import com.dolling.ui.another.MapActivity
import com.dolling.view_model.main.HistoryViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HistoryInProgressFragment(
    private val viewModel: HistoryViewModel
) : Fragment(), AdapterOnClickListener {

    private var _binding: FragmentHistoryInProgressBinding? = null
    private val binding get() = _binding!!
    private val adapter = HistoryAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryInProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvOrder.layoutManager = LinearLayoutManager(context)
        binding.rvOrder.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.rvOrder.adapter = adapter

        val isLoggedInWithEmail: Boolean
        requireContext().getSharedPreferences("dolling", Context.MODE_PRIVATE).apply {
            isLoggedInWithEmail = this.getBoolean("is_logged_in_with_email", false)
        }

        val userId =
            if (isLoggedInWithEmail) Firebase.auth.currentUser!!.email!! else Firebase.auth.currentUser!!.phoneNumber!!

        viewModel.getInProcessOrderList(userId)
        viewModel.inProcessOrderList.observe(viewLifecycleOwner, {
            it?.let { list ->
                adapter.setList(list)
                binding.textError.visibility = View.GONE
            }
            if (it.isNullOrEmpty()) {
                adapter.delList()
                binding.textError.visibility = View.VISIBLE
            }
        })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onAdapterParentViewClicked(_object: Any) {
        val order = _object as OrderModal
        Intent(requireContext(), MapActivity::class.java).apply {
            putExtra(MapActivity.EXTRA_SELLER_ID, order.seller_id)
            putExtra(MapActivity.EXTRA_ORDER_ID, order.id)
            startActivity(this)
        }
    }
}