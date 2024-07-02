package com.san.heartratemonitorwearos.view.screen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.san.heartratemonitorwearos.databinding.ActivityMonitoringBinding
import com.san.heartratemonitorwearos.service.HeartRateService

class MonitoringActivity : AppCompatActivity() {
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
            val intent = Intent(this, HeartRateService::class.java)
            stopService(intent)
        }
    }
}