package tn.turbodrive.presentation.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.turbodrive.R

class RideForegroundService : Service() {
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundWithNotification()
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundWithNotification() {
        ensureChannel()
        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.ride_tracking_notification_title))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.ride_tracking_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            )
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "tn.turbodrive.RIDE_TRACKING_START"
        const val ACTION_STOP = "tn.turbodrive.RIDE_TRACKING_STOP"
        private const val CHANNEL_ID = "ride_tracking"
        private const val NOTIFICATION_ID = 42001
    }
}
