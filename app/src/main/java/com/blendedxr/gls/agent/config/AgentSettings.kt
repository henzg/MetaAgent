package com.blendedxr.gls.agent.config

import android.content.Context

class AgentSettings(context: Context) {
    private val prefs =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var hubUrl: String?
        get() = prefs.getString(KEY_HUB_URL, null)
        set(value) = prefs.edit().putString(KEY_HUB_URL, value).apply()

    var deviceId: String?
        get() = prefs.getString(KEY_DEVICE_ID, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var deviceName: String?
        get() = prefs.getString(KEY_DEVICE_NAME, null)
        set(value) = prefs.edit().putString(KEY_DEVICE_NAME, value).apply()

    var autoLaunchPackage: String?
        get() = prefs.getString(KEY_AUTO_LAUNCH_PACKAGE, null)
        set(value) = prefs.edit().putString(KEY_AUTO_LAUNCH_PACKAGE, value).apply()

    val isConfigured: Boolean
        get() = !hubUrl.isNullOrBlank() && !deviceId.isNullOrBlank()

    companion object {
        private const val FILE_NAME = "agent_settings"
        // keys should align with MDM
        const val KEY_HUB_URL = "hubUrl"
        const val KEY_DEVICE_ID = "deviceId"
        const val KEY_DEVICE_NAME = "deviceName"
        const val KEY_AUTO_LAUNCH_PACKAGE = "autoLaunchPackage"
    }
}