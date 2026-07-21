package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkEntry
import com.example.viewmodel.SortOption
import com.example.viewmodel.WorkViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: WorkViewModel,
    onEntryClick: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val entries by viewModel.filteredEntries.collectAsState()
    val availableWorkTypes by viewModel.availableWorkTypes.collectAsState()

    // Filter flows from ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedWorkType by viewModel.selectedWorkTypeFilter.collectAsState()
    val dateStart by viewModel.dateRangeStart.collectAsState()
    val dateEnd by viewModel.dateRangeEnd.collectAsState()
    val currentSort by viewModel.sortOption.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var workTypeDropdownExpanded by remember { mutableStateOf(false) }

    val hasActiveFilters = searchQuery.isNotEmpty() || selectedStatus != null ||
            selectedWorkType != null || dateStart != null || dateEnd != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Work Orders",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier.testTag("filter_toggle_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("add_entry_fab")
                    .padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Work Order",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Block
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("search_customer_input"),
                placeholder = { Text("Search customer or phone...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Filtering drawer / toggle panel (Animated visibility)
            AnimatedVisibility(visible = showFilterSheet) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Filter & Refine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Row 1: Status Dropdown & Work Type Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Status Filter Selector
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { statusDropdownExpanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("filter_status_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = selectedStatus ?: "All Statuses",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = statusDropdownExpanded,
                                    onDismissRequest = { statusDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(Color.White)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                "All Statuses",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.selectedStatusFilter.value = null
                                            statusDropdownExpanded = false
                                        }
                                    )
                                    listOf("Pending Work", "Work Done", "Work Done - Payment Pending").forEach { status ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    status,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                ) 
                                            },
                                            onClick = {
                                                viewModel.selectedStatusFilter.value = status
                                                statusDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Work Type Filter Selector
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { workTypeDropdownExpanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("filter_work_type_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = selectedWorkType ?: "All Work Types",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = workTypeDropdownExpanded,
                                    onDismissRequest = { workTypeDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(Color.White)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                "All Work Types",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.selectedWorkTypeFilter.value = null
                                            workTypeDropdownExpanded = false
                                        }
                                    )
                                    availableWorkTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    type,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Medium
                                                ) 
                                            },
                                            onClick = {
                                                viewModel.selectedWorkTypeFilter.value = type
                                                workTypeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Row 2: Date Pickers (Start & End)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Date
                            OutlinedButton(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    dateStart?.let { cal.timeInMillis = it }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            cal.set(year, month, day, 0, 0, 0)
                                            viewModel.dateRangeStart.value = cal.timeInMillis
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_start_date_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(end = 2.dp)
                                )
                                Text(
                                    text = dateStart?.let { viewModel.formatDate(it) } ?: "Start Date",
                                    fontSize = 12.sp
                                )
                            }

                            // End Date
                            OutlinedButton(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    dateEnd?.let { cal.timeInMillis = it }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            cal.set(year, month, day, 0, 0, 0)
                                            viewModel.dateRangeEnd.value = cal.timeInMillis
                                        },
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("filter_end_date_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(end = 2.dp)
                                )
                                Text(
                                    text = dateEnd?.let { viewModel.formatDate(it) } ?: "End Date",
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Clear filters option
                        if (hasActiveFilters) {
                            TextButton(
                                onClick = { viewModel.clearFilters() },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("clear_filters_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterListOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All Filters")
                            }
                        }
                    }
                }
            }

            // Sorting Bar (Horizontal scrolling options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sort by:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SortOption.values().forEach { option ->
                        val isSelected = currentSort == option
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                                .background(containerColor)
                                .clickable { viewModel.sortOption.value = option }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("sort_chip_${option.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // Divider
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Scrollable list or empty state placeholder
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("empty_list_placeholder"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkOutline,
                            contentDescription = "No Work Orders",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Work Orders Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (hasActiveFilters) "Try resetting your filters or search query." else "Tap the orange + button below to create your first work order.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .testTag("work_entries_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        WorkEntryItem(
                            entry = entry,
                            viewModel = viewModel,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkEntryItem(
    entry: WorkEntry,
    viewModel: WorkViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine status badge colors
    val badgeBgColor = when (entry.status) {
        "Pending Work" -> Color(0xFFFEF3C7) // Amber light bg
        "Work Done" -> Color(0xFFD1FAE5) // Green light bg
        "Work Done - Payment Pending" -> Color(0xFFFEE2E2) // Red/orange light bg
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val badgeTextColor = when (entry.status) {
        "Pending Work" -> Color(0xFFD97706) // Darker Amber
        "Work Done" -> Color(0xFF059669) // Darker Green
        "Work Done - Payment Pending" -> Color(0xFFDC2626) // Darker Red
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("work_item_card_${entry.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Customer Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entry.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Work Type Subheader (with manual free text entry)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = entry.workType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Billed Amount
                Column {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatCurrency(entry.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Auto-Calculated Net Profit
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Profit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = viewModel.formatCurrency(entry.netProfit),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (entry.netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}
