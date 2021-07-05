package com.dolling.ui.main.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.dolling.databinding.FragmentHistoryContainerBinding
import com.dolling.view_model.main.HistoryViewModel

class HistoryContainerFragment : Fragment() {

    private var _binding: FragmentHistoryContainerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.buttonInProgress.isEnabled = false
                        binding.buttonDone.isEnabled = true
                    }
                    1 -> {
                        binding.buttonInProgress.isEnabled = true
                        binding.buttonDone.isEnabled = false
                    }
                }
            }
        }

        binding.viewPagerHistory.adapter = SectionPagerAdapter(this, viewModel)
        binding.viewPagerHistory.registerOnPageChangeCallback(viewPagerCallback)
    }

    override fun onDestroyView() {
        binding.viewPagerHistory.unregisterOnPageChangeCallback(viewPagerCallback)
        _binding = null
        super.onDestroyView()
    }
}