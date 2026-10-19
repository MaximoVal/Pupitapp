package com.notasparamibebe.app.network

import kotlinx.serialization.Serializable

@Serializable
data class NtfyMessage(
    val id: String = "",
    val time: Long = 0L,
    val event: String = "",
    val topic: String = "",
    val message: String? = null,
    val title: String? = null
)
