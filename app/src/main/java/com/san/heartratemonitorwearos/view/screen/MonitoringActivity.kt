package com.san.heartratemonitorwearos.view.screen

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.san.heartratemonitorwearos.Const
import com.san.heartratemonitorwearos.databinding.ActivityMonitoringBinding
import com.san.heartratemonitorwearos.service.HeartRateService

class MonitoringActivity : ComponentActivity() {
    private lateinit var binding: ActivityMonitoringBinding
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListener(this)
        initBroadCastReceiver()
    }

    private fun initListener(activity: Activity) {
        setBtnEndMonitoringListener(activity)
    }

    private fun setBtnEndMonitoringListener(activity: Activity) {
        binding.btnEndMonitoring.setOnClickListener {
            stopHeartRateService(activity)
            finish()
        }
    }

    private fun stopHeartRateService(activity: Activity) {
        val intent = Intent(activity, HeartRateService::class.java)

        stopService(intent)
    }

    private fun initBroadCastReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Const.ACTION_HEART_RATE_BROAD_CAST) {
                    binding.txtHeartRate.text = intent.getIntExtra(
                        Const.TAG_HEART_RATE_INTENT, 0
                    ).toString()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(Const.ACTION_HEART_RATE_BROAD_CAST), RECEIVER_NOT_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}