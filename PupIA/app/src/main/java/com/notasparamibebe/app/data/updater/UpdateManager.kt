package com.notasparamibebe.app.data.updater

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.notasparamibebe.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class UpdateManager(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        const val GITHUB_REPO_OWNER = "MaximoVal"
        const val GITHUB_REPO_NAME = "Pupitapp"
        private const val API_URL = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"

        fun isNewerVersion(current: String, candidate: String): Boolean {
            val cleanCurrent = current.trim().removePrefix("v").removePrefix("V")
            val cleanCandidate = candidate.trim().removePrefix("v").removePrefix("V")

            val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
            val candidateParts = cleanCandidate.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currentParts.size, candidateParts.size)
            for (i in 0 until maxLen) {
                val curr = currentParts.getOrElse(i) { 0 }
                val cand = candidateParts.getOrElse(i) { 0 }
                if (cand > curr) return true
                if (cand < curr) return false
            }
            return false
        }
    }

    suspend fun checkForUpdate(): Result<UpdateCheckResult> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(API_URL)
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "NotasParaMiBebe-Android")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 404) {
                        // Repositorio aún sin releases publicadas
                        return@withContext Result.success(
                            UpdateCheckResult(
                                isUpdateAvailable = false,
                                currentVersion = BuildConfig.VERSION_NAME,
                                newVersion = BuildConfig.VERSION_NAME,
                                downloadUrl = null,
                                releaseNotes = null
                            )
                        )
                    }
                    return@withContext Result.failure(Exception("Error al consultar GitHub: HTTP ${response.code}"))
                }

                val bodyString = response.body?.string() ?: return@withContext Result.failure(Exception("Respuesta vacía"))
                val release = json.decodeFromString<GitHubRelease>(bodyString)

                val currentVersion = BuildConfig.VERSION_NAME
                val candidateVersion = release.tagName.ifBlank { release.name ?: currentVersion }
                val hasUpdate = isNewerVersion(currentVersion, candidateVersion)

                // Buscar el primer asset .apk
                val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                val downloadUrl = apkAsset?.browserDownloadUrl

                Result.success(
                    UpdateCheckResult(
                        isUpdateAvailable = hasUpdate && !downloadUrl.isNullOrBlank(),
                        currentVersion = currentVersion,
                        newVersion = candidateVersion,
                        downloadUrl = downloadUrl,
                        releaseNotes = release.body
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(downloadUrl)
            .header("User-Agent", "NotasParaMiBebe-Android")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Error en descarga: HTTP ${response.code}"))
                }

                val responseBody = response.body ?: return@withContext Result.failure(Exception("Cuerpo de descarga vacío"))
                val totalLength = responseBody.contentLength()

                val destinationDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
                val apkFile = File(destinationDir, "notas_para_mi_bebe_update.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                responseBody.byteStream().use { input ->
                    FileOutputStream(apkFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (totalLength > 0) {
                                val progress = (totalBytesRead.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                                onProgress(progress)
                            }
                        }
                        output.flush()
                    }
                }

                // Iniciar instalación
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )

                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(installIntent)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
