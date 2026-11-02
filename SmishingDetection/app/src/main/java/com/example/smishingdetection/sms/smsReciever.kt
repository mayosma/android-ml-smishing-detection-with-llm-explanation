package com.example.smishingdetection.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.smishingdetection.ScanService

// Claude AI was used to assist in the implementation of the broadcast receiver.
// Any code generated was verified to ensure intended functionality is achieved
class smsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val fullMessage = messages.joinToString("") { it.displayMessageBody }

        val serviceIntent = Intent(context, ScanService::class.java).apply {
            putExtra("message_body", fullMessage)
        }
        context?.startForegroundService(serviceIntent)
    }

}
