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

class AgentService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotificaiton(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
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

    private fun buildNotificaiton(): Notification {
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
    companion object {
        private const val TAG = "MrCosmo:Service"
        private const val CHANNEL_ID = "agent"
        private const val NOTIFICATION_ID = 69
        private const val LAUNCH_DELAY_MS = 20_000L

        fun start(context: Context) {
            context.startForegroundService(
                Intent(
                    context, AgentService::class.java))
        }
    }
}