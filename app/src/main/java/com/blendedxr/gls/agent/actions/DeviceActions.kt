package com.blendedxr.gls.agent.actions

import android.content.Context
import com.blendedxr.gls.agent.accessibility.AgentAccessibilityService

class DeviceActions(private val context: Context) {
    fun launch(packageName: String): String {
        val result = AgentAccessibilityService.instance?.launchPackage(packageName)
        return when (result) {
            true -> "Launched $packageName"
            false -> "No launch intent for $packageName"
            null -> "Accessibility service not connected"
        }
    }
}