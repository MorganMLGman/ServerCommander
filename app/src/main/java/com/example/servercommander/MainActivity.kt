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

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager2 = findViewById(R.id.viewPager)

        sharedPref = getSharedPreferences(
            getString(R.string.app_name), Context.MODE_PRIVATE
        )

        with( sharedPref.edit()){
            putBoolean(getString(R.string.connectionTested), false)
            apply()
        }

        if ( sharedPref.contains(getString(R.string.server_url)) and
             sharedPref.contains(getString(R.string.username)) and
             sharedPref.contains(getString(R.string.pubkey)) and
             sharedPref.contains("server_type")
        ) {
            when (sharedPref.getString("server_type", "")) {
                "docker" -> {
                    viewPager2.adapter = DockerViewPagerAdapter(this)
                    tabLayout.getTabAt(2)?.text = "DOCKER"
                }
                "yunohost" -> {
                    viewPager2.adapter = YunohostViewPagerAdapter(this)
                    tabLayout.getTabAt(2)?.text = "YUNOHOST"
                }
                else -> {
                    finishAffinity()
                }
            }
        }
        else
        {
            openLoginActivityForResult()
        }

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
    }

    fun openLoginActivityForResult() {
        val intent = Intent(this, LoginActivity::class.java)
        resultLauncher.launch(intent)
    }

    var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data

            when (sharedPref.getString("server_type", "")) {
                "docker" -> {
                    viewPager2.adapter = DockerViewPagerAdapter(this)
                    tabLayout.getTabAt(2)?.text = "DOCKER"
                }
                "yunohost" -> {
                    viewPager2.adapter = YunohostViewPagerAdapter(this)
                    tabLayout.getTabAt(2)?.text = "YUNOHOST"
                }
                else -> {
                    finishAffinity()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

//        if (sharedPref.contains(getString(R.string.server_url)) and
//            sharedPref.contains(getString(R.string.username)) and
//            sharedPref.contains(getString(R.string.pubkey)) and
//            sharedPref.contains("server_type")
//        ) {
//            when (sharedPref.getString("server_type", "")) {
//                "docker" -> {
//                    viewPager2.adapter = DockerViewPagerAdapter(this)
//                    tabLayout.getTabAt(2)?.text = "DOCKER"
//                }
//                "yunohost" -> {
//                    viewPager2.adapter = YunohostViewPagerAdapter(this)
//                    tabLayout.getTabAt(2)?.text = "YUNOHOST"
//                }
//                else -> {
//                    finishAffinity()
//                }
//            }
//        }
//        else
//        {
//            openLoginActivityForResult()
//        }
    }
}