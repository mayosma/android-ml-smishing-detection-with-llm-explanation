package com.example.smishingdetection.ml

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.google.gson.Gson
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil

data class ClassificationResult(
    val hybrid: Float,
    val cnn: Float,
    val lstm: Float
)

data class PreprocessedInput(
    val textArray: Array<IntArray>,
    val urlArray: Array<IntArray>
)

class SmishingClassifier(private val context: Context) {
    private val interpreter: Interpreter

    init {
        val model = FileUtil.loadMappedFile(context, "model.tflite")
        interpreter = Interpreter(model)
    }

    // runs classifier based on input and provides classification output
    fun classify(textInput: Array<IntArray>, urlInput: Array<IntArray>): ClassificationResult {
        val inputs = mapOf(
            "lstm_input" to textInput,
            "cnn_input" to urlInput
        )
        val outputs: MutableMap<String, Any> = mutableMapOf(
            "output_0" to Array(1) { FloatArray(1) },  // confirm against Step 1's output — hybrid
            "output_1" to Array(1) { FloatArray(1) },  // cnn
            "output_2" to Array(1) { FloatArray(1) }   // lstm
        )

        interpreter.runSignature(inputs, outputs, "serving_default")

        return ClassificationResult(
            hybrid = (outputs["output_0"] as Array<FloatArray>)[0][0],
            cnn = (outputs["output_1"] as Array<FloatArray>)[0][0],
            lstm = (outputs["output_2"] as Array<FloatArray>)[0][0]
        )
    }
    // Claude AI was used to assist in integrating the preprocessing and classification process
    // any code that was generated was verified to ensure intended functionality was achieved

    // runs preprocessing of inputs before being classified
    fun preprocessAndClassify(text: String, url: String): ClassificationResult {
        val py = Python.getInstance()
        val result = py.getModule("preprocess").callAttr("preprocess", text, url)
        val parsed = Gson().fromJson(result.toString(), PreprocessedInput::class.java)
        return classify(parsed.textArray, parsed.urlArray)
    }

    fun close() = interpreter.close()
}