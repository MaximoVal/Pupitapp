package com.notasparamibebe.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notasparamibebe.app.NotesApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = NotesApplication.getRepository(context)
            receiverScope.launch {
                val secretCode = repository.secretCodeFlow.first()
                if (!secretCode.isNullOrBlank()) {
                    NtfyLiveService.startService(context)
                }
            }
        }
    }
}
