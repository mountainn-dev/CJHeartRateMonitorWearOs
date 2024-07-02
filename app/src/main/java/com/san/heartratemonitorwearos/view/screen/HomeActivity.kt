package com.san.heartratemonitorwearos.view.screen

import android.Manifest
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

        initListener()
        requestPermission()
    }
    
    private fun initListener() {
        setBtnStartMonitoringListener()
        setBtnSettingPermissionListener()
    }

    private fun setBtnStartMonitoringListener() {
        binding.btnStartMonitoring.setOnClickListener {
            val intent = Intent(this, HeartRateService::class.java)
            startService(intent)
        }
    }

    private fun setBtnSettingPermissionListener() {
        binding.btnSettingPermission.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
            }
            startActivity(intent)
        }
    }

    private fun requestPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
        requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.llRequestPermission.visibility = View.VISIBLE
        } else {
            binding.llRequestPermission.visibility = View.GONE
        }
    }
}