package com.blendedxr.gls.agent.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.blendedxr.gls.agent.service.AgentService

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "MrCosmo:Boot"
        private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
       if (context == null || intent?.action != Intent.ACTION_BOOT_COMPLETED)
       {
           return
       }

        val bootCompleteMessage = "Boot Complete"

        Log.i(TAG, bootCompleteMessage)
        AgentService.start(context)
    }

}