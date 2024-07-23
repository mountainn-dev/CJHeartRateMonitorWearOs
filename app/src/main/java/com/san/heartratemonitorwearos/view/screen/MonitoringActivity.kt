package com.san.heartratemonitorwearos.view.screen

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.wearable.Wearable
import com.san.heartratemonitorwearos.domain.utils.Const
import com.san.heartratemonitorwearos.data.repositoryimpl.HeartRateRepositoryImpl
import com.san.heartratemonitorwearos.databinding.ActivityMonitoringBinding
import com.san.heartratemonitorwearos.data.source.local.HeartRateSensorService
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService
import com.san.heartratemonitorwearos.domain.utils.Utils
import com.san.heartratemonitorwearos.domain.viewmodel.MonitoringViewModel
import com.san.heartratemonitorwearos.domain.viewmodelfactory.MonitoringViewModelFactory
import com.san.heartratemonitorwearos.domain.viewmodelimpl.MonitoringViewModelImpl
import kotlin.system.exitProcess

class MonitoringActivity : ComponentActivity() {
    private lateinit var binding: ActivityMonitoringBinding
    private lateinit var viewModel: MonitoringViewModel
    private lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = HeartRateRepositoryImpl(Utils.getRetrofit().create(HeartRateService::class.java))
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
        setBtnUrgentListener(activity)
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

    private fun setBtnUrgentListener(activity: Activity) {
        binding.btnUrgent.setOnClickListener {
            val task = LocationServices.getFusedLocationProviderClient(activity)

            if (Utils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, activity)
                && Utils.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, activity)) {
                task.lastLocation.addOnSuccessListener {
                    viewModel.urgent(it)
                    Log.d("location", "${it.latitude}, ${it.longitude}")
                }
            }
        }
    }

    private fun initBroadCastReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Const.ACTION_HEART_RATE_BROAD_CAST) {
                    val data = intent.getIntExtra(Const.TAG_HEART_RATE_INTENT, 0)
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

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    companion object {
        private const val SERVICE_SERVER_EXCEPTION_MESSAGE = "서버 상태가 불안정합니다."
    }
}