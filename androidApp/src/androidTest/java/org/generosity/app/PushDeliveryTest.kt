package org.generosity.app

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Opt-in live test: operator sends two notices only to this installation. Never logs tokens. */
class PushDeliveryTest {
    @Test fun receivesForegroundEventAndBackgroundTrayAlert() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("livePush") == "true")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val phase = File(context.filesDir, "push-test-phase")
        val tokenFile = File(context.filesDir, "push-test-token")
        val manager = context.getSystemService(NotificationManager::class.java)
        val app = FirebaseApp.initializeApp(context)
        assertEquals("generosity-v01", app?.options?.projectId)
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        AndroidPush.prepare(context)
        AndroidPush.signal.value = null
        val token = Tasks.await(FirebaseMessaging.getInstance().token, 60, TimeUnit.SECONDS)
        assertTrue(token.length >= 20)
        tokenFile.writeText(token)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                phase.writeText("foreground")
                await { AndroidPush.signal.value != null }
                val foreground = requireNotNull(AndroidPush.signal.value)
                assertFalse(foreground.open)
                assertTrue(manager.activeNotifications.isEmpty())
                scenario.onActivity { it.moveTaskToBack(true) }
                Thread.sleep(1500)
                phase.writeText("background")
                await { manager.activeNotifications.any { it.tag != foreground.noticeId } }
                assertTrue(manager.activeNotifications.any { it.notification.channelId == "urgent_needs" })
                phase.writeText("passed")
            } finally {
                tokenFile.delete()
                manager.cancelAll()
            }
        }
    }

    private fun await(condition: () -> Boolean) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(100)
        while (!condition() && System.nanoTime() < deadline) Thread.sleep(250)
        assertTrue("Expected live FCM delivery within the test window", condition())
    }
}
