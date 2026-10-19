package com.notasparamibebe.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,
    val content: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val isFavorite: Boolean = false
)
