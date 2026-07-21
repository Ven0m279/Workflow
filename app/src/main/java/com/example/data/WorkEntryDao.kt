package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {
    @Query("SELECT * FROM work_entries ORDER BY dateTimestamp DESC")
    fun getAllEntries(): Flow<List<WorkEntry>>

    @Query("SELECT * FROM work_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Int): WorkEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WorkEntry): Long

    @Update
    suspend fun updateEntry(entry: WorkEntry)

    @Delete
    suspend fun deleteEntry(entry: WorkEntry)
}
