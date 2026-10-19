package com.notasparamibebe.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notasparamibebe.app.NotesApplication
import com.notasparamibebe.app.data.local.Note
import com.notasparamibebe.app.data.repository.NotesRepository
import com.notasparamibebe.app.data.updater.UpdateCheckResult
import com.notasparamibebe.app.data.updater.UpdateManager
import com.notasparamibebe.app.service.NtfyLiveService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(
    application: Application,
    private val repository: NotesRepository,
    private val updateManager: UpdateManager = UpdateManager()
) : AndroidViewModel(application) {

    val secretCode: StateFlow<String?> = repository.secretCodeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayNotes: StateFlow<List<Note>> = repository.getTodayNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myNotes: StateFlow<List<Note>> = repository.getMyNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteNotes: StateFlow<List<Note>> = repository.getFavoriteNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estados de Autoactualización
    private val _updateInfo = MutableStateFlow<UpdateCheckResult?>(null)
    val updateInfo: StateFlow<UpdateCheckResult?> = _updateInfo.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
        _downloadProgress.value = null
    }

    fun checkForUpdates(silent: Boolean = true) {
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val result = updateManager.checkForUpdate()
            _isCheckingUpdate.value = false

            result.onSuccess { info ->
                if (info.isUpdateAvailable) {
                    _updateInfo.value = info
                } else if (!silent) {
                    _updateMessage.value = "¡Ya tienes la versión más reciente instalada! (${info.currentVersion})"
                }
            }.onFailure { error ->
                if (!silent) {
                    _errorMessage.value = error.localizedMessage ?: "No se pudo verificar actualizaciones"
                }
            }
        }
    }

    fun downloadAndInstallUpdate(context: Context, downloadUrl: String) {
        viewModelScope.launch {
            _downloadProgress.value = 0.01f
            val result = updateManager.downloadAndInstallApk(context, downloadUrl) { progress ->
                _downloadProgress.value = progress
            }

            result.onFailure { error ->
                _downloadProgress.value = null
                _errorMessage.value = "Error al descargar actualización: ${error.localizedMessage}"
            }
        }
    }

    fun connect(code: String, onSuccess: () -> Unit) {
        if (code.isBlank()) {
            _errorMessage.value = "Por favor ingresa un código válido"
            return
        }

        viewModelScope.launch {
            repository.saveSecretCode(code.trim())
            NtfyLiveService.startService(getApplication())
            onSuccess()
        }
    }

    fun disconnect(onDisconnected: () -> Unit) {
        viewModelScope.launch {
            repository.clearSecretCode()
            NtfyLiveService.stopService(getApplication())
            onDisconnected()
        }
    }

    fun sendNote(content: String, onSuccess: () -> Unit) {
        if (content.isBlank()) {
            _errorMessage.value = "La nota no puede estar vacía"
            return
        }

        viewModelScope.launch {
            _isSending.value = true
            _errorMessage.value = null
            val result = repository.sendNote(content)
            _isSending.value = false

            if (result.isSuccess) {
                onSuccess()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Error al enviar la nota"
            }
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.toggleFavorite(note.id, !note.isFavorite)
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = NotesApplication.getRepository(application)
                    return NotesViewModel(application, repo) as T
                }
            }
    }
}
