package com.blendedxr.gls.agent.accessibility

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

@SuppressLint("AccessibilityPolicy")
class AgentAccessibilityService : AccessibilityService() {

    companion object {
        const val TAG = "MrCosmo:Accessibility"

        @Volatile
        var instance: AgentAccessibilityService? = null
            private set
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) {
            return
        }

        val message = "pkg=${event.packageName}|class=${event.className}|eventType=${AccessibilityEvent.eventTypeToString(event.eventType)}"

        Log.i(TAG, message)

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val message = "Accessibility service has been connected to the Agent."
        Log.i(TAG, message)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    fun launchPackage(packageName: String): Boolean {
        val intent =packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            Log.w(TAG, "No launch intent for $packageName")
            return false
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Log.i(TAG, "startActivity sent for $packageName")
        return true
    }

    override fun onInterrupt() {}
}