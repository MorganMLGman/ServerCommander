package com.example.servercommander

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var myViewPagerAdapter: MyViewPagerAdapter

    private lateinit var sharedPref: SharedPreferences

    var resultLauncher = registerForActivityResult(StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK)
        {
            println("RESULT OK")
        }
        else
        {
            println("RESULT NOK")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPref = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager)
        myViewPagerAdapter = MyViewPagerAdapter(this)

        viewPager2.adapter = myViewPagerAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager2.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewPager2.registerOnPageChangeCallback( object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                tabLayout.getTabAt(position)!!.select()
            }
        })

        if ( !sharedPref.contains("serverUrl") or
             !sharedPref.contains("username") or
             !sharedPref.contains("pubkey")
        ) {
            val intent = Intent( this, LoginActivity::class.java).apply{}

            resultLauncher.launch(intent)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

}