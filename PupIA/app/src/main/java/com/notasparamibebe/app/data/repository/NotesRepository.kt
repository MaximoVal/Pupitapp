package com.notasparamibebe.app.data.repository

import com.notasparamibebe.app.data.local.Note
import com.notasparamibebe.app.data.local.NoteDao
import com.notasparamibebe.app.data.preferences.PreferenceRepository
import com.notasparamibebe.app.network.NtfyClient
import com.notasparamibebe.app.network.NtfyMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.UUID

class NotesRepository(
    private val noteDao: NoteDao,
    private val preferenceRepository: PreferenceRepository,
    private val ntfyClient: NtfyClient = NtfyClient()
) {

    val secretCodeFlow: Flow<String?> = preferenceRepository.secretCodeFlow

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()

    fun getMyNotes(): Flow<List<Note>> = noteDao.getNotesByOrigin(isFromMe = true)

    fun getPartnerNotes(): Flow<List<Note>> = noteDao.getNotesByOrigin(isFromMe = false)

    fun getFavoriteNotes(): Flow<List<Note>> = noteDao.getFavoriteNotes()

    fun getTodayNotes(): Flow<List<Note>> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return noteDao.getNotesFromToday(startOfDay)
    }

    suspend fun saveSecretCode(code: String) {
        preferenceRepository.saveSecretCode(code)
    }

    suspend fun clearSecretCode() {
        preferenceRepository.clearSecretCode()
    }

    suspend fun getSenderId(): String {
        return preferenceRepository.getOrCreateSenderId()
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        noteDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun deleteNote(id: String) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }

    suspend fun sendNote(content: String): Result<Unit> = withContext(Dispatchers.IO) {
        val secretCode = preferenceRepository.secretCodeFlow.first()
        if (secretCode.isNullOrBlank()) {
            return@withContext Result.failure(IllegalStateException("No hay un código secreto configurado"))
        }

        val senderId = preferenceRepository.getOrCreateSenderId()
        val noteId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Guardado optimista inmediato en Room
        val localNote = Note(
            id = noteId,
            content = content.trim(),
            timestamp = timestamp,
            isFromMe = true,
            isFavorite = false
        )
        noteDao.insertNote(localNote)

        // Enviar por ntfy.sh
        val topic = NtfyClient.sanitizeTopic(secretCode)
        ntfyClient.sendNote(topic, senderId, content.trim())
    }

    suspend fun processIncomingNtfyMessage(
        msg: NtfyMessage,
        onNotificationNeeded: ((Note) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val rawMessage = msg.message ?: return@withContext
        val parsed = NtfyClient.parsePayload(rawMessage) ?: return@withContext
        val (incomingSenderId, actualContent) = parsed

        val mySenderId = preferenceRepository.getOrCreateSenderId()
        if (incomingSenderId == mySenderId) {
            // Nota originada por este mismo dispositivo, ignorar
            return@withContext
        }

        // Si ya existe la nota con este ID, no duplicar
        if (noteDao.countById(msg.id) > 0) {
            return@withContext
        }

        val msgTimestamp = if (msg.time > 0) msg.time * 1000L else System.currentTimeMillis()
        val incomingNote = Note(
            id = msg.id,
            content = actualContent,
            timestamp = msgTimestamp,
            isFromMe = false,
            isFavorite = false
        )

        noteDao.insertNote(incomingNote)

        // Si la nota fue enviada hace menos de 5 minutos, disparar notificación local
        val isRecent = (System.currentTimeMillis() - msgTimestamp) < (5 * 60 * 1000L)
        if (isRecent) {
            onNotificationNeeded?.invoke(incomingNote)
        }
    }
}
