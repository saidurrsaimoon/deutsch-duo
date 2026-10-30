package com.saimoon.ourlife

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class OurLifeMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        saveToken(uid, token)
    }

    private fun saveToken(uid: String, token: String) {
        val tokenId = (uid + "_" + token.hashCode().toUInt().toString()).replace("/", "_")
        FirebaseFirestore.getInstance().collection("pushTokens").document(tokenId).set(
            mapOf("uid" to uid, "token" to token, "updatedAt" to System.currentTimeMillis())
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"] ?: message.notification?.title ?: "Our Little Life ♡"
        val body = message.data["body"] ?: message.notification?.body ?: "You have a new message"
        val channelId = "our_little_life_messages"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Our Little Life messages", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Private messages from your person"
                enableVibration(true)
                lightColor = Color.rgb(255, 130, 179)
            }
            manager.createNotificationChannel(channel)
        }
        val openChat = Intent(this, NativeHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("open_chat", true)
        }
        val pending = PendingIntent.getActivity(this, 410, openChat,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setColor(Color.rgb(184, 69, 121))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }
}