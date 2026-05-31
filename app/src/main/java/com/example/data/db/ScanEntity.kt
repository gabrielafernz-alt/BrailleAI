package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val textResult: String,
    val brailleCode: String, // Braille symbol patterns
    val timestamp: Long = System.currentTimeMillis(),
    val sourceImageName: String? = null, // Mock camera feed name or manual entry
    val isBookmarked: Boolean = false,
    val category: String = "General"
)
