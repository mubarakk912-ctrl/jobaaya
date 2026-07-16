package com.example.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.MainActivity
import com.example.R
import com.example.data.database.JobaayaDatabase
import com.example.data.model.SystemNotification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles incoming Firebase Cloud Messaging push notifications, and keeps the
 * FCM token synced to Firestore (users/{profileId}.fcmToken) so Cloud Functions
 * know where to deliver notifications for this profile.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "jobaaya_default_channel"
        const val CHANNEL_NAME = "Jobaaya Notifications"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Support both "notification" payload (sent from Firebase Console) and
        // "data" payload (sent from Cloud Functions) so either works.
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Jobaaya"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""

        showSystemNotification(title, body)
        saveNotificationLocally(title, body)
    }

    private fun openDatabase(): JobaayaDatabase {
        return Room.databaseBuilder(
            applicationContext,
            JobaayaDatabase::class.java,
            "jobaaya_database"
        ).fallbackToDestructiveMigration().build()
    }

    private fun saveTokenToFirestore(token: String) {
        serviceScope.launch {
            try {
                val db = openDatabase()
                val myProfile = db.userProfileDao.getMyProfileDirect()
                if (myProfile != null && myProfile.id.isNotBlank()) {
                    // Keep local Room copy in sync too
                    db.userProfileDao.updateProfile(myProfile.copy(fcmToken = token))

                    // Push just the token to Firestore so Cloud Functions can find it
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(myProfile.id)
                        .set(
                            mapOf(
                                "fcmToken" to token,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveNotificationLocally(title: String, body: String) {
        serviceScope.launch {
            try {
                val db = openDatabase()
                db.systemNotificationDao.insertNotification(
                    SystemNotification(title = title, content = body)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showSystemNotification(title: String, body: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Jobaaya app notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}