package com.blendedxr.gls.agent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.blendedxr.gls.agent.R
import com.blendedxr.gls.agent.accessibility.AgentAccessibilityService
import com.blendedxr.gls.agent.hub.HubClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AgentService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        scope.launch { hubClient.run() }
        Log.i(TAG, "AgentService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "AgentService started, pid=${android.os.Process.myPid()}")

        Handler(Looper.getMainLooper()).postDelayed({
            val ok = AgentAccessibilityService.instance?.launchPackage("com.ghostlight.novaxr")
            Log.i(TAG, "launch result: $ok")
        },LAUNCH_DELAY_MS)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        hubClient.stop()
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Agent", NotificationManager.IMPORTANCE_LOW
            )
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AgentService")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val hubClient = HubClient(HUB_URL, DEVICE_ID, DEVICE_NAME)
    companion object {
        private const val TAG = "MrCosmo:Service"
        private const val CHANNEL_ID = "agent"
        private const val NOTIFICATION_ID = 69
        private const val LAUNCH_DELAY_MS = 20_000L
        // hub
        private const val HUB_URL = "http://192.168.8.151:5000/hubs/headsetcontrol"
        private const val DEVICE_ID = "2G0YC5ZG9N02JR"
        private const val DEVICE_NAME = "Dev Quest"
        // methods
        fun start(context: Context) {
            context.startForegroundService(
                Intent(
                    context, AgentService::class.java))
        }

    }
}