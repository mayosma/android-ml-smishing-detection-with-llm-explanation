package com.example.smishingdetection.bridge

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.smishingdetection.llm.ExplanationGenerator
import com.example.smishingdetection.ml.SmishingClassifier
import com.example.smishingdetection.savedScores.savedScores
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WebAppInterface(
    private val context: Context,
    private val classifier: SmishingClassifier,
    private val explanationGenerator: ExplanationGenerator,
    private val coroutineScope: CoroutineScope,
    private val webView: WebView
) {
    @JavascriptInterface
    fun classifyMessage(text: String, url: String): String {
        val result = classifier.preprocessAndClassify(text, url)
        return Gson().toJson(result)
    }

    @JavascriptInterface
    fun explainMessage(text: String, hybridScore: Float, textScore: Float, urlScore: Float) {
        coroutineScope.launch {
            val explanation = explanationGenerator.explain(text, hybridScore, textScore, urlScore)
            withContext(Dispatchers.Main) {
                webView.evaluateJavascript(
                    "displayExplanation(${Gson().toJson(explanation)})", null
                )
            }
        }
    }
    @JavascriptInterface
    fun loadHistory(): String = Gson().toJson(savedScores.load(context))

    @JavascriptInterface
    fun removeHistoryItem(id: String) = savedScores.remove(context, id)
}