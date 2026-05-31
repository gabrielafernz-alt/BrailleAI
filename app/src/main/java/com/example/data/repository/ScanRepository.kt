package com.example.data.repository

import com.example.data.db.ScanDao
import com.example.data.db.ScanEntity
import kotlinx.coroutines.flow.Flow

class ScanRepository(private val scanDao: ScanDao) {
    val allScans: Flow<List<ScanEntity>> = scanDao.getAllScans()
    val bookmarkedScans: Flow<List<ScanEntity>> = scanDao.getBookmarkedScans()

    suspend fun insertScan(scan: ScanEntity): Long {
         return scanDao.insertScan(scan)
    }

    suspend fun updateScan(scan: ScanEntity) {
        scanDao.updateScan(scan)
    }

    suspend fun deleteScan(scanId: Long) {
        scanDao.deleteScan(scanId)
    }

    suspend fun clearAll() {
        scanDao.clearAll()
    }
}
