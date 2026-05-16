package tn.dadadrive.presentation.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import tn.dadadrive.app.MainActivity
import com.dadadrive.R
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@AndroidEntryPoint
class DriverPushMessagingService : FirebaseMessagingService() {
    @Inject lateinit var pushTokenRegistrar: PushTokenRegistrar
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val rideId = data["ride_id"]
        val pickup = data["pickup_address"] ?: message.notification?.body.orEmpty()
        val dropoff = data["dropoff_address"].orEmpty()
        val price = data["price"].orEmpty()
        val eta = data["estimated_minutes"].orEmpty()

        val hasOfferData = pickup.isNotBlank() || dropoff.isNotBlank() || price.isNotBlank() || eta.isNotBlank()
        if (!hasOfferData) return
        Log.i(TAG, "FCM offer received rideId=$rideId")

        ensureChannel()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(DriverNotificationBridge.EXTRA_OPEN_DRIVER_OFFER, true)
            if (!rideId.isNullOrBlank()) putExtra(DriverNotificationBridge.EXTRA_RIDE_ID, rideId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            5001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val lines = buildList {
            if (price.isNotBlank()) add(getString(R.string.driver_notification_offer_line_price, price))
            if (pickup.isNotBlank()) add(getString(R.string.driver_notification_offer_line_pickup, pickup))
            if (dropoff.isNotBlank()) add(getString(R.string.driver_notification_offer_line_dropoff, dropoff))
            if (eta.isNotBlank()) add(getString(R.string.driver_notification_offer_line_eta, eta))
        }
        val content = lines.joinToString("  |  ")
        val bigText = lines.joinToString("\n")

        val notification = NotificationCompat.Builder(this, OFFER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle(getString(R.string.driver_notification_offer_title))
            .setContentText(content.ifBlank { getString(R.string.driver_incoming_ride_title) })
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText.ifBlank { content }))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(OFFER_NOTIFICATION_ID, notification)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token received")
        pushTokenRegistrar.registerProvidedToken(serviceScope, token)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(OFFER_CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            OFFER_CHANNEL_ID,
            getString(R.string.driver_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    private companion object {
        private const val TAG = "DriverPushMessaging"
        private const val OFFER_CHANNEL_ID = "driver_ride_assigned"
        private const val OFFER_NOTIFICATION_ID = 41010
    }
}
