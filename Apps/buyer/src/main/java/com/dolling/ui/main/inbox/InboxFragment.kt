package com.dolling.ui.main.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dolling.adapter.InboxAdapter
import com.dolling.databinding.FragmentInboxBinding
import com.dolling.listeners.AdapterOnClickListener
import com.dolling.modal.InboxModal
import com.dolling.view_model.main.InboxViewModel

class InboxFragment :
    Fragment(),
    AdapterOnClickListener {

    private val viewModel: InboxViewModel by viewModels()
    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: InboxAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = InboxAdapter(this)
        binding.rvInbox.layoutManager = LinearLayoutManager(context)
        binding.rvInbox.adapter = adapter

        viewModel.getInboxListFromFirestore()
        viewModel.inboxList.observe(viewLifecycleOwner, { list ->
            if (!list.isNullOrEmpty())
                adapter.setList(list)
            else
                adapter.delList()
        })
    }

    override fun onAdapterParentViewClicked(_object: Any) {
        val inbox = _object as InboxModal
        InboxDetailDialogFragment(inbox).show(
            childFragmentManager, InboxDetailDialogFragment.TAG
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}