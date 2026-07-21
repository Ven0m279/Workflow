package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkDatabase
import com.example.data.WorkEntry
import com.example.data.WorkRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

enum class SortOption(val displayName: String) {
    BY_DATE("Date (Newest)"),
    BY_AMOUNT("Amount (Highest)"),
    BY_PENDING_PAYMENT("Pending Payment")
}

class WorkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkRepository

    init {
        val database = WorkDatabase.getDatabase(application)
        repository = WorkRepository(database.workEntryDao())
    }

    val rawEntries: StateFlow<List<WorkEntry>> = repository.allEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedStatusFilter = MutableStateFlow<String?>(null)
    val selectedWorkTypeFilter = MutableStateFlow<String?>(null)
    val dateRangeStart = MutableStateFlow<Long?>(null)
    val dateRangeEnd = MutableStateFlow<Long?>(null)
    val sortOption = MutableStateFlow(SortOption.BY_DATE)

    // Derived filtered and sorted entries
    @Suppress("UNCHECKED_CAST")
    val filteredEntries: StateFlow<List<WorkEntry>> = combine(
        rawEntries,
        searchQuery,
        selectedStatusFilter,
        selectedWorkTypeFilter,
        dateRangeStart,
        dateRangeEnd,
        sortOption
    ) { flowsArray ->
        val entries = flowsArray[0] as List<WorkEntry>
        val query = flowsArray[1] as String
        val status = flowsArray[2] as String?
        val workType = flowsArray[3] as String?
        val start = flowsArray[4] as Long?
        val end = flowsArray[5] as Long?
        val sort = flowsArray[6] as SortOption

        var result = entries

        // Search filter (customer name or mobile number)
        if (query.isNotEmpty()) {
            result = result.filter {
                it.customerName.contains(query, ignoreCase = true) ||
                it.mobileNumber.contains(query)
            }
        }

        // Status filter
        if (status != null) {
            result = result.filter { it.status == status }
        }

        // Work type filter
        if (workType != null) {
            result = result.filter { it.workType.equals(workType, ignoreCase = true) }
        }

        // Date range filter
        if (start != null) {
            result = result.filter { it.dateTimestamp >= start }
        }
        if (end != null) {
            // Include entire end day by checking up to end of that day
            result = result.filter { it.dateTimestamp <= end + 86399999L }
        }

        // Sorting
        when (sort) {
            SortOption.BY_DATE -> {
                result = result.sortedByDescending { it.dateTimestamp }
            }
            SortOption.BY_AMOUNT -> {
                result = result.sortedByDescending { it.amount }
            }
            SortOption.BY_PENDING_PAYMENT -> {
                // Prioritize "Work Done - Payment Pending" first, then by total amount
                result = result.sortedWith(
                    compareByDescending<WorkEntry> { it.status == "Work Done - Payment Pending" }
                        .thenByDescending { it.amount }
                )
            }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Unique list of work types used for filters
    val availableWorkTypes: StateFlow<List<String>> = rawEntries
        .map { list ->
            list.map { it.workType.trim() }
                .distinct()
                .filter { it.isNotEmpty() }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Summary calculations across all work entries (for the 5 summary cards on Home Screen)
    val totalWorkAmount: StateFlow<Double> = rawEntries.map { list -> list.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val materialCost: StateFlow<Double> = rawEntries.map { list -> list.sumOf { it.materialCost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val laborCost: StateFlow<Double> = rawEntries.map { list -> list.sumOf { it.laborCost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val pendingAmount: StateFlow<Double> = rawEntries.map { list ->
        list.filter { it.status == "Work Done - Payment Pending" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val profitAmount: StateFlow<Double> = rawEntries.map { list ->
        list.sumOf { it.amount } - list.sumOf { it.materialCost } - list.sumOf { it.laborCost }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // CRUD operations
    fun insertEntry(entry: WorkEntry, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertEntry(entry)
            onComplete(id)
        }
    }

    fun updateEntry(entry: WorkEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry)
        }
    }

    fun deleteEntry(entry: WorkEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun getEntryById(id: Int, onResult: (WorkEntry?) -> Unit) {
        viewModelScope.launch {
            val entry = repository.getEntryById(id)
            onResult(entry)
        }
    }

    fun saveUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "invoice_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val outputFile = File(context.filesDir, fileName)
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to format currency
    fun formatCurrency(amount: Double): String {
        return "₹%,.2f".format(Locale.US, amount)
    }

    // Helper to format date
    fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        return format.format(date)
    }

    // Clears all filters
    fun clearFilters() {
        searchQuery.value = ""
        selectedStatusFilter.value = null
        selectedWorkTypeFilter.value = null
        dateRangeStart.value = null
        dateRangeEnd.value = null
    }
}
