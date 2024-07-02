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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.san.heartratemonitorwearos.R
import com.san.heartratemonitorwearos.view.screen.MonitoringActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HeartRateService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    private lateinit var dataClient: DataClient
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        dataClient = Wearable.getDataClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL).also {
                if (it) startForeground(NOTIFICATION_ID, createNotification())
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0].toInt()
            scope.launch {
                sendHeartRate(heartRate)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private suspend fun sendHeartRate(heartRate: Int) {
        val request = PutDataMapRequest.create("/heartrate").apply {
            dataMap.putInt("heartrate", heartRate)
        }.asPutDataRequest().setUrgent()

        withContext(Dispatchers.IO) {
            try {
                dataClient.putDataItem(request)
                Log.d("HeartRateService", "Heart rate sent: $heartRate")
            } catch (e: Exception) {
                Log.e("HeartRateService", "Error sending heart rate", e)
            }
        }
    }

    private fun createNotification(): android.app.Notification {
        val channelId = "heart_rate_channel"
        val channelName = "Heart Rate Service"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MonitoringActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_CONTENT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_TITLE = "CJ 미래 기술 챌린지"
        private const val NOTIFICATION_CONTENT = "심박수 감지중"
    }
}