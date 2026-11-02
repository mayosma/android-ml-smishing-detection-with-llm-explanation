package com.example.smishingdetection

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView

@Composable
fun SmishingWebView(onWebViewReady: (WebView) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                loadUrl("file:///android_asset/web/index.html")
                onWebViewReady(this)
            }
        }
    )
}