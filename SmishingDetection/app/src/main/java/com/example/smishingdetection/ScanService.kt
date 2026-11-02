package com.example.smishingdetection

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat

import com.example.smishingdetection.llm.ExplanationGenerator
import com.example.smishingdetection.ml.SmishingClassifier
import com.example.smishingdetection.savedScores.ScanResult
import com.example.smishingdetection.savedScores.savedScores
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Claude AI was used to assist in the notification integrations
// any code generated was verified to ensure intended functionality was achieved
class ScanService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = intent?.getStringExtra("message_body") ?: return START_NOT_STICKY
        val url = Regex("(https?://\\S+)").find(message)?.value ?: ""

        val channelId = "scan_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Scanning", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Scanning message...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)

        CoroutineScope(Dispatchers.Default).launch {
            val classifier = SmishingClassifier(applicationContext)
            val result = classifier.preprocessAndClassify(message, url)
            val scanId = UUID.randomUUID().toString()
            val isRisky = result.hybrid > 0.40f

            var scanResult = ScanResult(
                scanId, message, result.lstm, result.cnn, result.hybrid,
                if (isRisky) "Generating explanation..." else "No significant risk detected."
            )
            savedScores.append(applicationContext, scanResult)
            sendBroadcast(Intent("SCAN_COMPLETED").setPackage(packageName))

            if (isRisky) {

                // sends notification if smishing message is detected
                val notification = NotificationCompat.Builder(this@ScanService, "smishing_alert_channel")
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("Possible smishing message detected")
                    .setContentText(message)
                    .build()
                if (ActivityCompat.checkSelfPermission(this@ScanService, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(this@ScanService).notify(System.currentTimeMillis().toInt(), notification)
                }

                val explanation = ExplanationGenerator(applicationContext).apply {
                    initialise()
                }.explain(message, result.hybrid, result.lstm, result.cnn)

                scanResult = scanResult.copy(explanation = explanation)
                savedScores.update(applicationContext, scanResult)
                sendBroadcast(Intent("SCAN_COMPLETED").setPackage(packageName))
            }

            classifier.close()
            stopSelf()
        }
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
