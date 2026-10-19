package com.notasparamibebe.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.notasparamibebe.app.NotesApplication
import com.notasparamibebe.app.network.NtfyClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class NtfyLiveService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var streamJob: Job? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var reconnectSignal: CompletableDeferred<Unit>? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        registerNetworkMonitoring()
        startListeningLoop()
    }

    private fun startAsForeground() {
        val notification = NotificationHelper.buildForegroundNotification(this)
        val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIFICATION_ID_FOREGROUND,
                notification,
                foregroundType
            )
        } catch (e: Exception) {
            // En versiones antiguas o restricciones de batería
            startForeground(NotificationHelper.NOTIFICATION_ID_FOREGROUND, notification)
        }
    }

    private fun registerNetworkMonitoring() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Notificar a la corrutina en espera para que reconecte inmediatamente
                reconnectSignal?.complete(Unit)
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (_: Exception) {}
    }

    private fun startListeningLoop() {
        streamJob = serviceScope.launch {
            val repository = NotesApplication.getRepository(this@NtfyLiveService)
            val ntfyClient = NotesApplication.getApplication().ntfyClient

            var backoffMs = 2000L

            while (isActive) {
                val secretCode = repository.secretCodeFlow.first()
                if (secretCode.isNullOrBlank()) {
                    // Si no hay código secreto configurado, detener el servicio
                    stopSelf()
                    break
                }

                val topic = NtfyClient.sanitizeTopic(secretCode)

                try {
                    // Escuchar el stream continuo
                    ntfyClient.listenToTopic(topic).collect { msg ->
                        // Al recibir un mensaje exitoso, reiniciamos el backoff
                        backoffMs = 2000L
                        repository.processIncomingNtfyMessage(msg) { incomingNote ->
                            NotificationHelper.showNewNoteNotification(this@NtfyLiveService, incomingNote)
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                }

                if (!isActive) break

                // Espera con backoff exponencial o hasta que vuelva la red
                val signal = CompletableDeferred<Unit>()
                reconnectSignal = signal
                withTimeoutOrNull(backoffMs) {
                    signal.await()
                }

                // Incrementar backoff: 2s -> 5s -> 10s -> max 30s
                backoffMs = when (backoffMs) {
                    2000L -> 5000L
                    5000L -> 10000L
                    10000L -> 20000L
                    else -> 30000L
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
        }
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_STOP_SERVICE = "com.notasparamibebe.app.ACTION_STOP_SERVICE"

        fun startService(context: Context) {
            val intent = Intent(context, NtfyLiveService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NtfyLiveService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
