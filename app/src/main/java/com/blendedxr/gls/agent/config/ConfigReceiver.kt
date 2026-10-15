package com.blendedxr.gls.agent.config

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.blendedxr.gls.agent.accessibility.AgentAccessibilityService
import com.blendedxr.gls.agent.hub.HubStatus

class ConfigReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CONFIGURE -> configure(context, intent)
            ACTION_STATUS -> reportStatus()
        }
        resultCode = Activity.RESULT_OK
    }

    private fun configure(context: Context, intent: Intent) {
        val settings = AgentSettings(context)

        intent.getStringExtra(AgentSettings.KEY_HUB_URL)?.let {
            settings.hubUrl = it
        }
        intent.getStringExtra(AgentSettings.KEY_DEVICE_ID)?.let {
            settings.deviceId = it
        }
        intent.getStringExtra(AgentSettings.KEY_DEVICE_NAME)?.let {
            settings.deviceName = it
        }
        intent.getStringExtra(AgentSettings.KEY_AUTO_LAUNCH_PACKAGE)?.let{
            settings.autoLaunchPackage = it
        }
        Log.i(TAG, "Configured: hub=${settings.hubUrl}|id=${settings.deviceId}|" +
                "name=${settings.deviceName}|waitingroom=${settings.autoLaunchPackage}")
    }

    private fun reportStatus() {
        val hub = if (HubStatus.connected) 1 else 0
        val accessibility = if (AgentAccessibilityService.instance != null)
            1 else 0

        // MDMs check-in step search for this
        resultData = "hub=$hub accessibility=$accessibility"
    }
    companion object {
        private const val TAG = "MrCosmo:ConfigReceiver"
        const val ACTION_CONFIGURE = "com.blendedxr.gls.agent.CONFIGURE"
        const val ACTION_STATUS = "com.blendedxr.gls.agent.STATUS"
    }


}