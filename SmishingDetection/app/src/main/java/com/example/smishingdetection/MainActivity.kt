package com.example.smishingdetection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.smishingdetection.bridge.WebAppInterface
import com.example.smishingdetection.llm.ExplanationGenerator
import com.example.smishingdetection.ml.SmishingClassifier
import com.example.smishingdetection.ui.theme.SmishingDetectionTheme
import kotlinx.coroutines.launch

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.webkit.WebView

import androidx.activity.result.contract.ActivityResultContracts

import android.app.NotificationChannel
import android.app.NotificationManager

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_SMS_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        requestSmsPermission()

        val classifier = SmishingClassifier(applicationContext)
        val explanationGenerator = ExplanationGenerator(applicationContext)

        lifecycleScope.launch {
            explanationGenerator.initialise()
        }

// Claude AI was used to assist in context setting
        setContent {
            SmishingDetectionTheme {
                SmishingWebView { webView ->
                    webViewRef = webView
                    webView.addJavascriptInterface(
                        WebAppInterface(applicationContext, classifier, explanationGenerator, lifecycleScope, webView),
                        "Android"
                    )
                }
            }
        }

        // notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val channel = NotificationChannel(
            "smishing_alert_channel",
            "Smishing Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    private fun requestSmsPermission() {
        val permission = Manifest.permission.RECEIVE_SMS
        val grant = ContextCompat.checkSelfPermission(this, permission)
        if (grant != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_SMS_PERMISSION)
        }
    }

    private var webViewRef: WebView? = null   // ← class-level, accessible everywhere in this class

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            webViewRef?.evaluateJavascript(
                "refreshHistory();",
                null
            )
        }
    }
    // Claude AI was used to assist in integrating receiver functionality when resuming and pausing the receiver
    override fun onResume() {
        super.onResume()
        webViewRef?.evaluateJavascript("refreshHistory()", null);
        ContextCompat.registerReceiver(
            this,
            scanReceiver,
            IntentFilter("SCAN_COMPLETED"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        unregisterReceiver(scanReceiver)
        super.onPause()
    }
}