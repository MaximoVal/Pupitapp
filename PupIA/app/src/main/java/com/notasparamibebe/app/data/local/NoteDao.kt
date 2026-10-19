package com.notasparamibebe.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isFromMe = :isFromMe ORDER BY timestamp DESC")
    fun getNotesByOrigin(isFromMe: Boolean): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isFromMe = 0 AND timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun getNotesFromToday(startOfDay: Long): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNote(note: Note): Long

    @Query("UPDATE notes SET isFavorite = :isFavorite WHERE id = :id")
    fun updateFavoriteStatus(id: String, isFavorite: Boolean): Int

    @Query("SELECT COUNT(*) FROM notes WHERE id = :id")
    fun countById(id: String): Int

    @Query("DELETE FROM notes WHERE id = :id")
    fun deleteNoteById(id: String): Int
}
