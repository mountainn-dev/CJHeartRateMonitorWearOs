package com.san.heartratemonitorwearos.data.source.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.san.heartratemonitorwearos.domain.utils.Const
import com.san.heartratemonitorwearos.R
import com.san.heartratemonitorwearos.data.entity.HeartRateEntity
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateDataService
import com.san.heartratemonitorwearos.data.source.remote.retrofit.HeartRateService
import com.san.heartratemonitorwearos.domain.utils.Utils
import com.san.heartratemonitorwearos.view.screen.HomeActivity
import com.san.heartratemonitorwearos.view.screen.MonitoringActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class HeartRateSensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var heartRateService: HeartRateService
    private lateinit var heartRateDataService: HeartRateDataService
    private lateinit var foregroundNotificationBuilder: NotificationCompat.Builder
    private lateinit var thresholdNotificationBuilder: NotificationCompat.Builder
    private var heartRateSensor: Sensor? = null
    private val heartRateData = mutableListOf<Int>()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val broadCastIntent = Intent(Const.ACTION_HEART_RATE_BROAD_CAST)

    override fun onCreate() {
        super.onCreate()

        initManagers()
        initApiService()
        initSensor()
        initNotification()
    }

    private fun initManagers() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun initApiService() {
        heartRateService = Utils.getRetrofit("http://49.247.41.208:8080").create(HeartRateService::class.java)
        heartRateDataService = Utils.getRetrofit("http://49.247.47.116:8082").create(HeartRateDataService::class.java)

        // updateWorkNow
    }

    private fun initSensor() {
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }

    private fun initNotification() {
        notificationManager.createNotificationChannel(heartRateServiceNotificationChannel())

        // 포그라운드, 임계치 관련 알림 작성
        foregroundNotificationBuilder = foregroundNotificationBuilder()
        thresholdNotificationBuilder = thresholdNotificationBuilder()
    }

    private fun heartRateServiceNotificationChannel() = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
    )

    /**
     * fun foregroundNotificationBuilder()
     *
     * 포그라운드 서비스 제공을 위한 고정 알림
     * 심박수가 감지되는 동안 고정 Notification 제공
     */
    private fun foregroundNotificationBuilder() = NotificationCompat.Builder(
        this,
        NOTIFICATION_CHANNEL_ID
    )
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(FOREGROUND_NOTIFICATION_CONTENT)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(pendingIntent())

    /**
     * fun thresholdNotificationBuilder()
     *
     * 심박수 임계치 초과 알림
     */
    private fun thresholdNotificationBuilder() = NotificationCompat.Builder(
        this,
        NOTIFICATION_CHANNEL_ID
    )
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(THRESHOLD_NOTIFICATION_CONTENT)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(pendingIntent())

    /**
     * fun pendingIntent()
     *
     * 포그라운드 알림 및 임계치 초과 알림 누를 경우 모니터링 화면으로 안내
     */
    private fun pendingIntent(): PendingIntent? {
        val intent = Intent(this@HeartRateSensorService, MonitoringActivity::class.java).apply {
            flags =  Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(Intent(this@HeartRateSensorService, HomeActivity::class.java))
            addNextIntent(intent) // Use the intent with flags
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!serviceRunning) {
            registerHeartRateListener(this)
            serviceRunning = true
        }
        return START_STICKY
    }

    private fun registerHeartRateListener(listener: SensorEventListener) {
        heartRateSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI).also {
                // 리스너 등록 성공 이후 포그라운드 서비스 제공
                if (it) startForeground(FOREGROUND_NOTIFICATION_ID, foregroundNotificationBuilder.build())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            sendHeartRateBroadCast(heartRate)
            Log.d("heartRate", heartRate.toString())

            heartRateData.add(heartRate)
            if (heartRateData.size == MAX_HEART_RATE_DATA_COUNT) sendHeartRateData(HeartRateEntity(avgHeartRateData()))
            if (heartRate > HEART_RATE_THRESHOLD) {
                notificationManager.notify(THRESHOLD_NOTIFICATION_ID, thresholdNotificationBuilder.build())
            }
        }
    }

    private fun sendHeartRateBroadCast(heartRate: Int) {
        broadCastIntent.putExtra(Const.TAG_HEART_RATE_INTENT, heartRate)
        sendBroadcast(broadCastIntent)
    }

    private fun sendHeartRateData(entity: HeartRateEntity) {
        scope.launch {
            heartRateData.clear()
            setHeartRate(entity)
        }
    }

    private fun avgHeartRateData(): Int {
        heartRateData.filter { it != 0 }.average().toInt()
        val zeroRemovedHeartRate = heartRateData.filter { it != 0 }

        return if (zeroRemovedHeartRate.isNotEmpty()) zeroRemovedHeartRate.average().toInt()
        else 0
    }

    private suspend fun setHeartRate(entity: HeartRateEntity) {
        try {
            heartRateDataService.setHeartRate(entity)
            Log.d("setHeartRate", "success")
        } catch (e: Exception) {
            Log.d("setHeartRateException", e.message ?: e.toString())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceRunning = false
        scope.cancel()
    }

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val THRESHOLD_NOTIFICATION_ID = 2
        private const val NOTIFICATION_CHANNEL_ID = "heart_rate_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Heart Rate Service"
        private const val NOTIFICATION_TITLE = "CJ 심박수 모니터"
        private const val FOREGROUND_NOTIFICATION_CONTENT = "심박수 감지중"
        private const val THRESHOLD_NOTIFICATION_CONTENT = "현재 심박수가 너무 높습니다. 잠시 휴식을 취하세요."
        private const val HEART_RATE_THRESHOLD = 100
        private const val MAX_HEART_RATE_DATA_COUNT = 3
        private var serviceRunning = false
    }
}