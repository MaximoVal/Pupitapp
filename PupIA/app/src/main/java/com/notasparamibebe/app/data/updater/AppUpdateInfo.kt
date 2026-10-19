package com.notasparamibebe.app.data.updater

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubAsset(
    val name: String = "",
    @SerialName("browser_download_url")
    val browserDownloadUrl: String = "",
    val size: Long = 0
)

@Serializable
data class GitHubRelease(
    @SerialName("tag_name")
    val tagName: String = "",
    val name: String? = null,
    val body: String? = null,
    val prerelease: Boolean = false,
    val assets: List<GitHubAsset> = emptyList()
)

data class UpdateCheckResult(
    val isUpdateAvailable: Boolean,
    val currentVersion: String,
    val newVersion: String,
    val downloadUrl: String?,
    val releaseNotes: String?
)
