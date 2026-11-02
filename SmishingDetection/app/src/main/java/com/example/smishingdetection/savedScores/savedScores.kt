package com.example.smishingdetection.savedScores

import android.content.Context
import com.google.gson.Gson
import java.io.File

data class ScanResult(
    val id: String,
    val text: String,
    val textScore: Float,
    val urlScore: Float,
    val hybridScore: Float,
    val explanation: String
)

object savedScores {
    private fun file(context: Context) =
        File(context.filesDir, "scan_history.json")

    fun load(context: Context): MutableList<ScanResult> {
        val file = file(context)
        if (!file.exists()) return mutableListOf()
        return Gson().fromJson(
            file.readText(),
            Array<ScanResult>::class.java
        ).toMutableList()
    }

    fun append(context: Context, result: ScanResult) {
        val scores = load(context)
        scores.add(0, result)
        file(context).writeText(Gson().toJson(scores))
    }

    fun remove(context: Context, id: String) {
        val scores = load(context)
        scores.removeAll { it.id == id }
        file(context).writeText(Gson().toJson(scores))
    }

    fun update(context: Context, updated: ScanResult) {
        val scores = load(context).map { if (it.id == updated.id) updated else it }
        file(context).writeText(Gson().toJson(scores))
    }
}

