package com.dolling.ui.main.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.dolling.databinding.FragmentInboxDetailDialogBinding
import com.dolling.modal.InboxModal
import com.dolling.utils.Utils.countDaysTimestamp

class InboxDetailDialogFragment(
    private val inbox: InboxModal
) : DialogFragment() {

    private var _binding: FragmentInboxDetailDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TAG = "tag"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInboxDetailDialogBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = inbox.title
        val time = countDaysTimestamp(inbox.timestamp.toDate())
        binding.timestampText.text = time
        binding.messageText.text = inbox.message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}