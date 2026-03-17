package com.rjs.smsforward

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rjs.smsforward.screens.AboutFragment
import com.rjs.smsforward.screens.LogsFragment
import com.rjs.smsforward.screens.RuleListFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RuleListFragment()
            1 -> LogsFragment()
            2 -> AboutFragment()
            else -> RuleListFragment()
        }
    }
}
