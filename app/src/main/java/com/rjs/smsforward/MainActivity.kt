package com.rjs.smsforward

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.rjs.smsforward.screens.RuleListFragment
import com.rjs.smsforward.support.Constants
import org.json.JSONObject
import java.net.URL

class MainActivity : BaseActivity() {

    private var isRefresh = false
    private lateinit var addFab: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var viewPagerAdapter: ViewPagerAdapter? = null

    private val tabTitles = arrayOf("Rules", "Logs", "About")
    private val tabIcons = intArrayOf(R.drawable.ic_tab_rules, R.drawable.ic_tab_logs, R.drawable.ic_tab_about)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        addFab = findViewById(R.id.add_fab)
        addFab.setOnClickListener {
            showBottomSheetDialog(false, -1)
        }

        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
            tab.setIcon(tabIcons[position])
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                addFab.visibility = if (position == 0) View.VISIBLE else View.GONE
                recordScreenView(tabTitles[position])
            }
        })

        recordScreenView("Rules")
        checkForUpdate()
    }

    override fun onStart() {
        super.onStart()
        if (!isRefresh) {
            isRefresh = true
            checkSMSPermission()
        }
    }

    override fun ruleListUpdate(rlActionType: Constants.RLActionType, position: Int, smsRule: SMSRuleData?) {
        try {
            supportFragmentManager.fragments.forEach { fragment ->
                if (fragment is RuleListFragment) {
                    fragment.onRuleListUpdate(rlActionType, position, smsRule)
                    return@forEach
                }
                // ViewPager2 nests fragments inside a host fragment
                fragment.childFragmentManager.fragments.forEach { child ->
                    if (child is RuleListFragment) {
                        child.onRuleListUpdate(rlActionType, position, smsRule)
                        return@forEach
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkForUpdate() {
        Thread {
            try {
                val configUrl = "https://raw.githubusercontent.com/rjstech2020/smsforwarder/main/config.json"
                val response = URL(configUrl).readText()
                val config = JSONObject(response)
                val minVersion = config.optInt("min_version", 0)
                val message = config.optString("update_message", "Please update the app to continue.")
                val appVersion = packageManager.getPackageInfo(packageName, 0).versionCode

                if (appVersion < minVersion) {
                    runOnUiThread {
                        AlertDialog.Builder(this)
                            .setTitle("Update Required")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("Download Update") { _, _ ->
                                startActivity(Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/rjstech2020/smsforwarder/releases")))
                                finish()
                            }
                            .show()
                    }
                }
            } catch (_: Exception) {
                // Silently fail — app continues normally if network is unavailable
            }
        }.start()
    }

    private fun checkSMSPermission() {
        val permissions = mutableListOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                Constants.SMS_READ_PERMISSION_REQUEST_CODE
            )
        }
    }
}
