package com.san.heartratemonitorwearos.view.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.san.heartratemonitorwearos.BuildConfig
import com.san.heartratemonitorwearos.databinding.ActivityHomeBinding
import com.san.heartratemonitorwearos.service.HeartRateService

class HomeActivity : ComponentActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener(this)
        requestPermission()
    }
    
    private fun initListener(activity: Activity) {
        setBtnStartMonitoringListener(activity)
        setBtnSettingPermissionListener()
    }

    private fun setBtnStartMonitoringListener(activity: Activity) {
        binding.btnStartMonitoring.setOnClickListener {
            startHeartRateService(activity)
            sendUserToMonitoringScreen(activity)
        }
    }

    private fun startHeartRateService(activity: Activity) {
        val intent = Intent(activity, HeartRateService::class.java)

        startService(intent)
    }

    private fun sendUserToMonitoringScreen(activity: Activity) {
        val intent = Intent(activity, MonitoringActivity::class.java)

        startActivity(intent)
    }

    private fun setBtnSettingPermissionListener() {
        binding.btnSettingPermission.setOnClickListener {
            sendUserToPermissionSettingScreen()
        }
    }

    private fun sendUserToPermissionSettingScreen() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        }

        startActivity(intent)
    }

    private fun requestPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.BODY_SENSORS
        ))
    }

    override fun onResume() {
        super.onResume()
        checkPermission(this)
    }

    private fun checkPermission(activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.llRequestPermission.visibility = View.VISIBLE
        } else {
            binding.llRequestPermission.visibility = View.GONE
        }
    }
}