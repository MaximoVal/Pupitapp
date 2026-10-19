package com.notasparamibebe.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.notasparamibebe.app.MainActivity
import com.notasparamibebe.app.R
import com.notasparamibebe.app.data.local.Note

object NotificationHelper {

    const val CHANNEL_LIVE_CONNECTION = "live_connection_channel"
    const val CHANNEL_NOTES = "notes_channel"

    const val NOTIFICATION_ID_FOREGROUND = 1001
    const val NOTIFICATION_ID_NOTE = 2001

    const val EXTRA_OPEN_ROUTE = "extra_open_route"
    const val EXTRA_NOTE_ID = "extra_note_id"
    const val ACTION_MARK_FAVORITE = "com.notasparamibebe.app.ACTION_MARK_FAVORITE"
    const val ACTION_REPLY = "com.notasparamibebe.app.ACTION_REPLY"
    const val KEY_REPLY_TEXT = "key_reply_text"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Canal silencioso para ForegroundService persistente
            val liveChannel = NotificationChannel(
                CHANNEL_LIVE_CONNECTION,
                "Conexión en Vivo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene la sincronización en vivo con tu pareja"
                setShowBadge(false)
            }

            // 2. Canal prioritario para notas nuevas entrantes
            val notesChannel = NotificationChannel(
                CHANNEL_NOTES,
                "Nuevas Notas de tu Pareja",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de notas románticas recién recibidas"
                enableVibration(true)
                setShowBadge(true)
            }

            manager.createNotificationChannel(liveChannel)
            manager.createNotificationChannel(notesChannel)
        }
    }

    fun buildForegroundNotification(context: Context): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_LIVE_CONNECTION)
            .setContentTitle("PupIA")
            .setContentText("Conectado en vivo con tu bebé 💞")
            .setSmallIcon(android.R.drawable.ic_menu_send)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showNewNoteNotification(context: Context, note: Note) {
        // Tocar la notificación abre "Notas de hoy"
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_ROUTE, "list/today")
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            note.id.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Acción 1: Marcar como favorita
        val favoriteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_FAVORITE
            putExtra(EXTRA_NOTE_ID, note.id)
        }
        val favoritePendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.hashCode() + 1,
            favoriteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val favoriteAction = NotificationCompat.Action.Builder(
            android.R.drawable.btn_star_big_on,
            "❤️ Favorita",
            favoritePendingIntent
        ).build()

        // Acción 2: Responder en línea (RemoteInput)
        val remoteInput = RemoteInput.Builder(KEY_REPLY_TEXT)
            .setLabel("Escribe tu respuesta...")
            .build()

        val replyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REPLY
            putExtra(EXTRA_NOTE_ID, note.id)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            note.id.hashCode() + 2,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "💬 Responder",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val notification = NotificationCompat.Builder(context, CHANNEL_NOTES)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("Nueva nota de tu bebé")
            .setContentText(note.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(note.content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(favoriteAction)
            .addAction(replyAction)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_NOTE, notification)
        } catch (_: SecurityException) {
            // Permiso denegado por el usuario
        }
    }
}
