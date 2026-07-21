package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_entries")
data class WorkEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // Format: e.g. Jul 21, 2026 or YYYY-MM-DD
    val dateTimestamp: Long, // Epoch millis for sorting
    val customerName: String,
    val mobileNumber: String,
    val locationAddress: String,
    val referenceName: String,
    val workType: String,
    val amount: Double,
    val materialCost: Double,
    val laborCost: Double,
    val notes: String = "",
    val status: String, // "Pending Work", "Work Done", "Work Done - Payment Pending"
    val invoicePhotos: List<String> = emptyList() // List of local photo file-paths/URIs
) {
    // Net profit (auto-calculated: Amount - Material cost - Labor cost)
    val netProfit: Double
        get() = amount - materialCost - laborCost
}
