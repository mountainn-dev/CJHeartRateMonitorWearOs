package com.san.heartratemonitorwearos.service

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
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.san.heartratemonitorwearos.Const
import com.san.heartratemonitorwearos.R
import com.san.heartratemonitorwearos.view.screen.HomeActivity
import com.san.heartratemonitorwearos.view.screen.MonitoringActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class HeartRateService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private val broadCastIntent = Intent(Const.ACTION_HEART_RATE_BROAD_CAST)

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
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
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL).also {
                // 리스너 등록 성공한 경우에만 알림 제공
                if (it) startForeground(NOTIFICATION_ID, createNotification())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            sendHeartRateBroadCast(heartRate)
        }
    }

    private fun sendHeartRateBroadCast(heartRate: Int) {
        broadCastIntent.putExtra(Const.TAG_HEART_RATE_INTENT, heartRate)
        sendBroadcast(broadCastIntent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    /**
     * fun createNotification()
     *
     * 포그라운드 서비스 제공을 위한 고정 알림
     * 심박수가 감지되는 동안 Notification 제공
     */
    private fun createNotification(): android.app.Notification {
        val channelId = NOTIFICATION_CHANNEL_ID
        val channelName = NOTIFICATION_CHANNEL_NAME
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent())
            .build()
    }

    private fun pendingIntent(): PendingIntent? {
        val intent = Intent(this@HeartRateService, MonitoringActivity::class.java).apply {
            flags =  Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(Intent(this@HeartRateService, HomeActivity::class.java))
            addNextIntent(intent) // Use the intent with flags
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceRunning = false
        scope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "heart_rate_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Heart Rate Service"
        private const val NOTIFICATION_TITLE = "CJ 미래 기술 챌린지"
        private const val NOTIFICATION_CONTENT = "심박수 감지중"
        private var serviceRunning = false
    }
}