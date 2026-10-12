package com.blendedxr.gls.agent.capabilities

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.view.accessibility.AccessibilityManager
import com.blendedxr.gls.agent.accessibility.AgentAccessibilityService
import com.blendedxr.gls.agent.admin.AdminReceiver

class CapabilityChecker(private val context: Context) {
    // member variables
    private val policyManager = context.getSystemService(DevicePolicyManager::class.java)
    private val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
    private val adminComponent = ComponentName(context, AdminReceiver::class.java)

    fun checkAll(): List<Capability> = listOf(checkDeviceAdmin(), checkAccessibility(),)

    // helper functs to populate checkAll
    private fun checkDeviceAdmin(): Capability {
        val isAdmin = policyManager.isAdminActive(adminComponent)
        val deviceAdminName = "Your Agent"
        val deviceAdminDetail = if(isAdmin) "MrCosmo is your Admin" else "MrCosmo is under privileged."
        return Capability(name = deviceAdminName, ok = isAdmin, detail = deviceAdminDetail)
    }

    private fun checkAccessibility(): Capability {
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        val serviceComponent = ComponentName(context, AgentAccessibilityService::class.java)
        val isAccessibilityEnabled = enabledServices.any { info ->
            ComponentName(info.resolveInfo.serviceInfo.packageName, info.resolveInfo.serviceInfo.name) ==
                    serviceComponent
        }
        val accessibilityName = "Security Clearance"
        val accessibilityDetail = if(isAccessibilityEnabled) "Accessibility Enabled" else "Accessibility Disabled"

        return Capability(name = accessibilityName, ok = isAccessibilityEnabled, detail = accessibilityDetail)
    }

}