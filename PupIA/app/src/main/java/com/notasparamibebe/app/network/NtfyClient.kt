package com.notasparamibebe.app.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class NtfyClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    private val streamingClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep connection open indefinitely
        .pingInterval(30, TimeUnit.SECONDS)   // Keep-alive HTTP/2 pings
        .retryOnConnectionFailure(true)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val BASE_URL = "https://ntfy.sh"
        private const val TOPIC_PREFIX = "notas_bebe_"

        fun sanitizeTopic(rawCode: String): String {
            val cleaned = rawCode.trim().lowercase().replace(Regex("[^a-z0-9_-]"), "_")
            val valid = if (cleaned.isBlank()) "default" else cleaned
            return "$TOPIC_PREFIX$valid"
        }

        fun parsePayload(payload: String): Pair<String, String>? {
            val delimiterIndex = payload.indexOf("||")
            if (delimiterIndex == -1) return null
            val senderId = payload.substring(0, delimiterIndex)
            val content = payload.substring(delimiterIndex + 2)
            return Pair(senderId, content)
        }

        fun buildPayload(senderId: String, content: String): String {
            return "$senderId||$content"
        }
    }

    suspend fun sendNote(topic: String, senderId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        val payload = buildPayload(senderId, content)
        val requestBody = payload.toRequestBody("text/plain; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$BASE_URL/$topic")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Error HTTP ${response.code}: ${response.message}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToTopic(topic: String): Flow<NtfyMessage> = callbackFlow {
        val request = Request.Builder()
            .url("$BASE_URL/$topic/json?since=all")
            .build()

        val call = streamingClient.newCall(request)

        try {
            val response: Response = call.execute()
            if (!response.isSuccessful) {
                close(IOException("Error HTTP conectando a stream: ${response.code}"))
                return@callbackFlow
            }

            val source = response.body?.source()
            if (source == null) {
                close(IOException("Respuesta sin cuerpo para el stream"))
                return@callbackFlow
            }

            while (!isClosedForSend) {
                val line = source.readUtf8Line() ?: break
                if (line.isNotBlank()) {
                    try {
                        val ntfyMessage = json.decodeFromString<NtfyMessage>(line)
                        if (ntfyMessage.event == "message" && !ntfyMessage.message.isNullOrBlank()) {
                            trySend(ntfyMessage)
                        }
                    } catch (_: Exception) {
                        // Ignorar líneas malformadas o mensajes keep-alive
                    }
                }
            }
            close()
        } catch (e: Exception) {
            if (e !is CancellationException) {
                close(e)
            }
        }

        awaitClose {
            call.cancel()
        }
    }.flowOn(Dispatchers.IO)
}
