package com.blendedxr.gls.agent

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.os.Build
import android.widget.Button
import com.blendedxr.gls.agent.capabilities.CapabilityChecker
import com.blendedxr.gls.agent.service.AgentService

class MainActivity : Activity() {

    private lateinit var checker: CapabilityChecker
    private lateinit var statusText: TextView

    private fun refresh() {
        val capabilities = checker.checkAll()

        statusText.text = capabilities.joinToString("\n") { capability ->
            val mark = if (capability.ok) "Y" else "N"
            "$mark ${capability.name}: ${capability.detail}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.refresh_button).setOnClickListener{refresh()}

        checker = CapabilityChecker(context = this)
        statusText = findViewById<TextView>(R.id.status_text)

        AgentService.start(this)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }
}