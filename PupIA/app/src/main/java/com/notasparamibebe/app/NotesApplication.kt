package com.notasparamibebe.app

import android.app.Application
import android.content.Context
import com.notasparamibebe.app.data.local.AppDatabase
import com.notasparamibebe.app.data.preferences.PreferenceRepository
import com.notasparamibebe.app.data.repository.NotesRepository
import com.notasparamibebe.app.network.NtfyClient
import com.notasparamibebe.app.service.NotificationHelper
import com.notasparamibebe.app.service.NtfyLiveService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotesApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val preferenceRepository by lazy { PreferenceRepository(this) }
    val ntfyClient by lazy { NtfyClient() }
    val repository by lazy {
        NotesRepository(
            noteDao = database.noteDao(),
            preferenceRepository = preferenceRepository,
            ntfyClient = ntfyClient
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        NotificationHelper.createNotificationChannels(this)

        // Si ya hay un código secreto configurado, levantar el servicio en vivo
        applicationScope.launch {
            val secretCode = preferenceRepository.secretCodeFlow.first()
            if (!secretCode.isNullOrBlank()) {
                NtfyLiveService.startService(this@NotesApplication)
            }
        }
    }

    companion object {
        private lateinit var instance: NotesApplication

        fun getApplication(): NotesApplication = instance
        fun getRepository(context: Context): NotesRepository =
            (context.applicationContext as NotesApplication).repository
    }
}
