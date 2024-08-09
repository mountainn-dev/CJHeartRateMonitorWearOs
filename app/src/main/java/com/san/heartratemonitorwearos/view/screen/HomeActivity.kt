package com.san.heartratemonitorwearos.view.screen

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataClient.OnDataChangedListener
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.san.heartratemonitorwearos.BuildConfig
import com.san.heartratemonitorwearos.data.repositoryimpl.HeartRateRepositoryImpl
import com.san.heartratemonitorwearos.databinding.ActivityHomeBinding
import com.san.heartratemonitorwearos.data.source.local.HeartRateSensorService
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService
import com.san.heartratemonitorwearos.domain.state.UiState
import com.san.heartratemonitorwearos.domain.utils.Const
import com.san.heartratemonitorwearos.domain.utils.Utils
import com.san.heartratemonitorwearos.view.viewmodel.HomeViewModel
import com.san.heartratemonitorwearos.view.viewmodelfactory.HomeViewModelFactory
import com.san.heartratemonitorwearos.view.viewmodelfactory.MonitoringViewModelFactory
import com.san.heartratemonitorwearos.view.viewmodelimpl.HomeViewModelImpl
import com.san.heartratemonitorwearos.view.viewmodelimpl.MonitoringViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.create

class HomeActivity : ComponentActivity(), OnDataChangedListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var dataClient: DataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = HeartRateRepositoryImpl(Utils.getRetrofit("http://49.247.41.208:8080", null).create(
            HeartRateService::class.java))
        viewModel = ViewModelProvider(this, HomeViewModelFactory(repo)).get(
            HomeViewModelImpl::class.java)

        initObserver(this)
        initListener(this)
        requestPermission()
    }

    private fun initObserver(activity: Activity) {
        viewModel.state.observe(
            activity as LifecycleOwner,
            stateObserver(activity)
        )
    }

    private fun stateObserver(activity: Activity) = Observer<UiState> {
        when(it) {
            UiState.Success -> {
                startHeartRateService(activity)
                sendUserToMonitoringScreen(activity)
            }
            UiState.Loading -> {}
            UiState.Timeout -> {}
            UiState.ServiceError -> {
                Toast.makeText(activity, SERVICE_ERROR_MESSAGE, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun initListener(activity: Activity) {
        setDataListener(activity)
        setBtnStartMonitoringListener()
        setBtnSettingPermissionListener()
        setBtnSettingBatterySaverModeListener()
    }

    private fun setDataListener(activity: Activity) {
        dataClient = Wearable.getDataClient(activity)
    }

    private fun setBtnStartMonitoringListener() {
        binding.btnStartMonitoring.setOnClickListener {
            viewModel.updateWorkStatus()
        }
    }

    private fun startHeartRateService(activity: Activity) {
        val intent = Intent(activity, HeartRateSensorService::class.java)
        intent.putExtra(Const.TAG_ID_TOKEN, viewModel.idToken)

        startService(intent)
    }

    private fun sendUserToMonitoringScreen(activity: Activity) {
        val intent = Intent(activity, MonitoringActivity::class.java)
        intent.putExtra(Const.TAG_ID_TOKEN, viewModel.idToken)
        intent.putExtra(Const.TAG_USER_ID, viewModel.userId)

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

    private fun setBtnSettingBatterySaverModeListener() {
        binding.btnSettingBatterySaverMode.setOnClickListener {
            sendUserToBatterySaverSettingScreen()
        }
    }

    private fun requestPermission() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
        requestPermissionLauncher.launch(arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    /**
     * fun sendUserToBatterySaverSettingScreen()
     *
     * Doze mode 에서도 포그라운드 서비스 기능 실시간 제공을 위한 절전 모드 관련 설정
     * 절전 모드 및 절전 상태 해제
     * 실시간 통신을 위해서는 필수이나, 서비스 관련 필수 설정은 아니기 때문에 optional
     */
    private fun sendUserToBatterySaverSettingScreen() {
        val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)

        startActivity(intent)
    }

    /**
     * onDataChanged()
     *
     * Wear Data Layer api 를 사용하여 모바일 디바이스로부터 id 토큰과 user 아이디를 초기화
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        var token = NO_DATA
        var id = NO_DATA

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                event.dataItem.also { item ->
                    if (item.uri.path == "/userData") {
                        Log.d("data", "userData")
                        DataMapItem.fromDataItem(item).dataMap.apply {
                            token = getString(BuildConfig.APPLICATION_ID + Const.TAG_ID_TOKEN) ?: ""
                            id = getString(BuildConfig.APPLICATION_ID + Const.TAG_USER_ID) ?: ""
                        }
                    }
                }
            }
        }

        // 뷰모델 초기화
        if (token != NO_DATA && id != NO_DATA) {
            val repo = HeartRateRepositoryImpl(Utils.getRetrofit("http://49.247.41.208:8080", token).create(
                HeartRateService::class.java))
            viewModel = ViewModelProvider(this, HomeViewModelFactory(repo)).get(
                HomeViewModelImpl::class.java)
            viewModel.setUserData(token, id)

            Toast.makeText(this, String.format(USER_DATA_CONNECTION_MESSAGE, id), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkEssentialSettings(this)
        Wearable.getDataClient(this).addListener(this)
    }

    private fun checkEssentialSettings(activity: Activity) {
        if (essentialSettings(activity)) {
            binding.llRequestPermission.visibility = View.GONE
            binding.btnStartMonitoring.visibility = View.VISIBLE
        } else {
            binding.btnStartMonitoring.visibility = View.GONE
            binding.llRequestPermission.visibility = View.VISIBLE
        }
    }

    /**
     * fun essentialSettings()
     *
     * 필수 설정 확인
     * 1. 센서 접근 권한
     * 2. 알림 권한
     * 3. 위치 권한
     */
    private fun essentialSettings(activity: Activity) =
        Utils.checkPermission(
            Manifest.permission.BODY_SENSORS, activity
        ) && Utils.checkPermission(
            Manifest.permission.POST_NOTIFICATIONS, activity
        ) && Utils.checkPermission(
            Manifest.permission.ACCESS_FINE_LOCATION, activity
        ) && Utils.checkPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION, activity
        )

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    companion object {
        private const val SERVICE_ERROR_MESSAGE = "서비스 요청에 실패하였습니다."
        private const val USER_DATA_CONNECTION_MESSAGE = "연결 성공 - %s"
        private const val NO_DATA = ""
    }
}