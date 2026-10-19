package com.notasparamibebe.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.notasparamibebe.app.NotesApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val repository = NotesApplication.getRepository(context)

        when (intent.action) {
            NotificationHelper.ACTION_MARK_FAVORITE -> {
                val noteId = intent.getStringExtra(NotificationHelper.EXTRA_NOTE_ID) ?: return
                receiverScope.launch {
                    repository.toggleFavorite(noteId, true)
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "Guardada en tus favoritas ❤️", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            NotificationHelper.ACTION_REPLY -> {
                val remoteInput = RemoteInput.getResultsFromIntent(intent)
                val replyText = remoteInput?.getCharSequence(NotificationHelper.KEY_REPLY_TEXT)?.toString()

                if (!replyText.isNullOrBlank()) {
                    val pendingResult = goAsync()
                    receiverScope.launch {
                        try {
                            repository.sendNote(replyText)

                            // Actualizar la notificación informando del envío exitoso
                            val repliedNotification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_NOTES)
                                .setSmallIcon(android.R.drawable.ic_dialog_email)
                                .setContentTitle("Notas para mi bebe")
                                .setContentText("Respuesta enviada: \"$replyText\" ❤️")
                                .setPriority(NotificationCompat.PRIORITY_LOW)
                                .setAutoCancel(true)
                                .setTimeoutAfter(3000)
                                .build()

                            try {
                                NotificationManagerCompat.from(context).notify(
                                    NotificationHelper.NOTIFICATION_ID_NOTE,
                                    repliedNotification
                                )
                            } catch (_: SecurityException) {}

                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, "Respuesta enviada ❤️", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }
    }
}
