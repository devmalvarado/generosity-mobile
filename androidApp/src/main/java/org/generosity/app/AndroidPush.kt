package org.generosity.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.MutableStateFlow

data class PushSignal(val accountId: String, val noticeId: String, val open: Boolean) {
    companion object {
        fun parse(account: String?, notice: String?, open: Boolean): PushSignal? {
            if (account?.matches(Regex("[1-9][0-9]{0,18}")) != true || notice?.matches(Regex("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}")) != true) return null
            return PushSignal(account, notice, open)
        }
    }
}

object AndroidPush {
    val signal = MutableStateFlow<PushSignal?>(null)
    val token = MutableStateFlow<String?>(null)

    fun prepare(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("urgent_needs", context.getString(R.string.urgent_notifications), NotificationManager.IMPORTANCE_HIGH))
    }

    fun update(context: Context, enabled: Boolean, app: GenerosityAppState) {
        val configured = runCatching { FirebaseApp.initializeApp(context) != null }.getOrDefault(false)
        val manager = NotificationManagerCompat.from(context)
        val allowed = manager.areNotificationsEnabled() &&
            context.getSystemService(NotificationManager::class.java).getNotificationChannel("urgent_needs")?.importance != NotificationManager.IMPORTANCE_NONE
        app.setPushDevice(token.value, "android", enabled && allowed)
        if (!enabled) manager.cancelAll()
        if (!configured) return
        val messaging = FirebaseMessaging.getInstance()
        messaging.isAutoInitEnabled = enabled && allowed
        if (enabled && allowed) messaging.token.addOnSuccessListener {
            token.value = it
        }
    }

    fun opened(intent: Intent): PushSignal? = PushSignal.parse(intent.getStringExtra("account_id"), intent.getStringExtra("notice_id"), true)
}

class GenerosityMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) { AndroidPush.token.value = token }
    override fun onMessageReceived(message: RemoteMessage) {
        // Foreground: reconcile the owned inbox; Firebase handles background tray alerts.
        AndroidPush.signal.value = PushSignal.parse(message.data["account_id"], message.data["notice_id"], false)
    }
}
