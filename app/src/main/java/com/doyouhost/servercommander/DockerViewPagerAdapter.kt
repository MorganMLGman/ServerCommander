package com.doyouhost.servercommander

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.doyouhost.servercommander.fragments.*

class DockerViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> SystemFragment()
            2 -> DockerFragment()
            3 -> TerminalFragment()
            4 -> SettingsFragment()
            else -> HomeFragment()
        }
    }

}