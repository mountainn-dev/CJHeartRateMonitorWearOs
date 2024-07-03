package com.san.heartratemonitorwearos.view.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import com.san.heartratemonitorwearos.databinding.ActivityMonitoringBinding
import com.san.heartratemonitorwearos.service.HeartRateService

class MonitoringActivity : ComponentActivity() {
    private lateinit var binding: ActivityMonitoringBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener()
    }

    private fun initListener() {
        setBtnEndMonitoringListener()
    }

    private fun setBtnEndMonitoringListener() {
        binding.btnEndMonitoring.setOnClickListener {
            stopHeartRateService()
            finish()
        }
    }

    private fun stopHeartRateService() {
        val service = Intent(this, HeartRateService::class.java)

        stopService(service)
    }

    private fun sendUserToHomeScreen() {
        val activity = Intent(this, HomeActivity::class.java)

        startActivity(activity)
    }
}