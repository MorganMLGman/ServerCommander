package com.doyouhost.servercommander

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
    }

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
            "ServerCommander", Context.MODE_PRIVATE
        )

        with( sharedPref.edit()){
            putBoolean("connectionTested", false)
            apply()
        }

        if ( sharedPref.contains("serverUrl") and
             sharedPref.contains("username") and
             sharedPref.contains("sshPort") and
             sharedPref.contains("pubkey") and
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

        checkPermission(Manifest.permission.POST_NOTIFICATIONS, NOTIFICATION_PERMISSION_CODE)

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

    private fun openLoginActivityForResult() {
        val intent = Intent(this, LoginActivity::class.java)
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
//            val data: Intent? = result.data

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


    override fun onDestroy() {
        with(NotificationManagerCompat.from(this)){
            cancel(0)
        }
        super.onDestroy()
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            NotificationHandler.createNotificationChannel(this@MainActivity)
            NotificationHandler.showNotification(this@MainActivity)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
                NotificationHandler.createNotificationChannel(this@MainActivity)
                NotificationHandler.showNotification(this@MainActivity)
            } else {
                Toast.makeText(this@MainActivity, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}