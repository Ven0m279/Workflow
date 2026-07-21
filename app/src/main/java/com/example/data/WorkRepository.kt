package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkRepository(private val workEntryDao: WorkEntryDao) {
    val allEntries: Flow<List<WorkEntry>> = workEntryDao.getAllEntries()

    suspend fun getEntryById(id: Int): WorkEntry? {
        return workEntryDao.getEntryById(id)
    }

    suspend fun insertEntry(entry: WorkEntry): Long {
        return workEntryDao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: WorkEntry) {
        workEntryDao.updateEntry(entry)
    }

    suspend fun deleteEntry(entry: WorkEntry) {
        workEntryDao.deleteEntry(entry)
    }
}
