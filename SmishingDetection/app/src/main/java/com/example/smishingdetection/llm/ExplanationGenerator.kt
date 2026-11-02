package com.example.smishingdetection.llm

import android.content.Context
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import java.io.File
import dev.ffmpegkit.llama.LlamaModel
// code was inspired from: https://github.com/ffmpegkit-maintained/llama-android
// Claude AI was used to assist in integrating the initialisation functionality
class ExplanationGenerator(private val context: Context) {
    private var model: LlamaModel? = null

    // Initialises model instant
    suspend fun initialise() {
        val modelPath = File(context.getExternalFilesDir("models"), "model.gguf").absolutePath
        val availableThreads = Runtime.getRuntime().availableProcessors()
        model = Llama.loadModel(
            modelPath = modelPath,
            config = LlamaConfig(contextSize = 512, threads = 2)
        )
    }
    // generates explanation
    suspend fun explain(messageText: String, hybridScore: Float, textScore: Float, urlScore: Float) : String {
        val currentModel = model ?: throw IllegalStateException("Model not loaded yet")
        val result = Llama.complete(
            currentModel,

            prompt = """
        Analyse this message:

        Message: $messageText

        Smishing confidence: ${(hybridScore * 100).toInt()}%
        Text-only smishing confidence: ${(textScore * 100).toInt()}%
        URL smishing confidence: ${(urlScore * 100).toInt()}%
        
        """.trimIndent(),

            systemPrompt = """
        You are a smishing security analyst.

        Analyse ONLY evidence present in the message. Do not invent details.

        Identify only observed:
        - impersonation
        - urgency/fear
        - requests for credentials, payment or personal information
        - suspicious URLs/domains
        - unusual wording
        - attempts to bypass normal security procedures
   

        Explain simply for a non-technical user.

        Output exactly with simple formatting:

        SUMMARY:
        [1-2 sentences]

        INDICATORS:
        - [observed indicator]

        ACTIONS:
        - [practical safety action]

        Do not output anything else.
        
        """.trimIndent(),

            maxTokens = 256
        )

        val cleanedText = result.text
            .replace(Regex("<think>[\\s\\S]*?</think>"), "")
            .trim()

        return cleanedText

    }





}