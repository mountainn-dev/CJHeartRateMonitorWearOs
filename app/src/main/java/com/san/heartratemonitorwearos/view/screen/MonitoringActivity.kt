package com.san.heartratemonitorwearos.view.screen

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.san.heartratemonitorwearos.Const
import com.san.heartratemonitorwearos.data.repositoryimpl.HeartRateRepositoryImpl
import com.san.heartratemonitorwearos.databinding.ActivityMonitoringBinding
import com.san.heartratemonitorwearos.data.source.local.HeartRateSensorService
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService
import com.san.heartratemonitorwearos.domain.Utils
import com.san.heartratemonitorwearos.viewmodel.MonitoringViewModel
import com.san.heartratemonitorwearos.viewmodelfactory.MonitoringViewModelFactory
import com.san.heartratemonitorwearos.viewmodelimpl.MonitoringViewModelImpl

class MonitoringActivity : ComponentActivity() {
    private lateinit var binding: ActivityMonitoringBinding
    private lateinit var viewModel: MonitoringViewModel
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = HeartRateRepositoryImpl(Utils.getRetrofit("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MTIxMiIsImlzcyI6ImVsbGlvdHRfa2ltIiwiZXhwIjoxNzUzMjg4MDA5LCJpYXQiOjE3MjE3NTIwMDl9.8t8FqLRkeUO1fWTLO9Ucbc20GwusxEhx7CmOWYgTktg").create(HeartRateService::class.java))
        viewModel = ViewModelProvider(this, MonitoringViewModelFactory(repo)).get(MonitoringViewModelImpl::class.java)

        initObserver(this)
        initListener(this)
        initBroadCastReceiver()
    }

    private fun initObserver(activity: Activity) {
        viewModel.viewModelError.observe(
            activity as LifecycleOwner,
            viewModelErrorObserver(activity)
        )
    }

    private fun viewModelErrorObserver(
        activity: Activity
    ) = Observer<Boolean> {
        if (it) Toast.makeText(activity, SERVICE_SERVER_EXCEPTION_MESSAGE, Toast.LENGTH_SHORT).show()
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
        val intent = Intent(activity, HeartRateSensorService::class.java)

        stopService(intent)
    }

    private fun initBroadCastReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Const.ACTION_HEART_RATE_BROAD_CAST) {
                    val data = intent.getIntExtra(Const.TAG_HEART_RATE_INTENT, 0)
                    viewModel.addHeartRateData(data)
                    binding.txtHeartRate.text = data.toString()
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

    companion object {
        private const val SERVICE_SERVER_EXCEPTION_MESSAGE = "서버 상태가 불안정합니다."
    }
}