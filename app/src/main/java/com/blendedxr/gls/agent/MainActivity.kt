package com.blendedxr.gls.agent

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.os.Build

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val status = findViewById<TextView>(R.id.status_text)

        status.text = "$packageName\nAdnroid SDK ${Build.VERSION.SDK_INT}\nHello Agent"
    }
}