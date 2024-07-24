package com.san.heartratemonitorwearos.view.screen

import android.Manifest
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.san.heartratemonitorwearos.data.repositoryimpl.HeartRateRepositoryImpl
import com.san.heartratemonitorwearos.data.source.local.HeartRateSensorService
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService
import com.san.heartratemonitorwearos.databinding.ActivityMonitoringBinding
import com.san.heartratemonitorwearos.domain.utils.Const
import com.san.heartratemonitorwearos.domain.utils.Utils
import com.san.heartratemonitorwearos.domain.viewmodel.MonitoringViewModel
import com.san.heartratemonitorwearos.domain.viewmodelfactory.MonitoringViewModelFactory
import com.san.heartratemonitorwearos.domain.viewmodelimpl.MonitoringViewModelImpl

class MonitoringActivity : ComponentActivity() {
    private lateinit var binding: ActivityMonitoringBinding
    private lateinit var viewModel: MonitoringViewModel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = HeartRateRepositoryImpl(Utils.getRetrofit("http://43.203.200.27:8080").create(HeartRateService::class.java))
        viewModel = ViewModelProvider(this, MonitoringViewModelFactory(repo)).get(MonitoringViewModelImpl::class.java)

        initObserver(this)
        initListener(this)
        initBroadCastReceiver()
        initLocationSetting(this)
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
        if (it) Toast.makeText(activity, REQUEST_FAIL_MESSAGE, Toast.LENGTH_SHORT).show()
        else Toast.makeText(activity, REQUEST_SUCCESS_MESSAGE, Toast.LENGTH_SHORT).show()
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
            if (Utils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, activity)
                && Utils.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, activity)) {
                locationClient.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        viewModel.urgent(it)
                    } else {
                        locationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
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
                    viewModel.setHeartRate(data)
                }
            }
        }

        registerReceiver(receiver, IntentFilter(Const.ACTION_HEART_RATE_BROAD_CAST), RECEIVER_NOT_EXPORTED)
    }

    private fun initLocationSetting(activity: Activity) {
        locationClient = LocationServices.getFusedLocationProviderClient(activity)
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {}
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    companion object {
        private const val REQUEST_SUCCESS_MESSAGE = "요청에 성공하였습니다."
        private const val REQUEST_FAIL_MESSAGE = "요청에 실패하였습니다."
    }
}