package com.dolling.ui.main.history

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dolling.view_model.main.HistoryViewModel

class SectionPagerAdapter(
    fragment: Fragment,
    private val viewModel: HistoryViewModel
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HistoryInProgressFragment(viewModel)
            1 -> HistoryDoneFragment(viewModel)
            else -> Fragment()
        }
    }
}