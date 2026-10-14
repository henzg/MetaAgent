package com.blendedxr.gls.agent.hub

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class HubClient(private val url: String,
                private val deviceId: String,
                private val deviceName: String) {

    private suspend fun connect() {
        try {
            connection.start().await()
            connection.invoke("Hello", deviceId, deviceName, "").await()
            Log.i(TAG, "Connected to hub and said Hello")
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            Log.e(TAG, "Hub connection failed: ${e.message}", e)
            connection.stop()
        }
    }

    suspend fun run() {
        while (true) {
            if (connection.connectionState != HubConnectionState.CONNECTED) {
                connect()
            }
            delay(RETRY_DELAY)
        }
    }
    fun stop() {
        connection.stop()
    }

    private val connection: HubConnection = HubConnectionBuilder.create(url).build()
    companion object {
        private const val TAG = "MrCosmo:Hub"
        private val RETRY_DELAY = 5.seconds
    }
}