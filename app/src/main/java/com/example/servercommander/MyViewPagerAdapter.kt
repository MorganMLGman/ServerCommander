package com.example.servercommander

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.servercommander.fragments.HomeFragment
import com.example.servercommander.fragments.SettingsFragment
import com.example.servercommander.fragments.SystemFragment
import com.example.servercommander.fragments.TerminalFragment

class MyViewPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> HomeFragment()
            1 -> SystemFragment()
            2 -> TerminalFragment()
            3 -> SettingsFragment()
            else -> HomeFragment()
        }
    }

}