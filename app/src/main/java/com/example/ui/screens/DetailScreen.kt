package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.WorkEntry
import com.example.viewmodel.WorkViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: WorkViewModel,
    entryId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var entry by remember { mutableStateOf<WorkEntry?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPhotoForZoom by remember { mutableStateOf<String?>(null) }

    // Load entry details
    LaunchedEffect(entryId) {
        viewModel.getEntryById(entryId) { result ->
            entry = result
        }
    }

    // Refresh detail state when db entries update (e.g. status changes)
    val rawEntries by viewModel.rawEntries.collectAsState()
    LaunchedEffect(rawEntries) {
        viewModel.getEntryById(entryId) { result ->
            entry = result
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Work Order Details",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { entry?.id?.let { onEdit(it) } }, modifier = Modifier.testTag("detail_edit_top_button")) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Work Order",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        val currentEntry = entry
        if (currentEntry == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header details
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentEntry.customerName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = currentEntry.workType,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Quick status indicator
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (currentEntry.status) {
                                            "Pending Work" -> Color(0xFFFEF3C7)
                                            "Work Done" -> Color(0xFFD1FAE5)
                                            "Work Done - Payment Pending" -> Color(0xFFFEE2E2)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = currentEntry.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentEntry.status) {
                                        "Pending Work" -> Color(0xFFD97706)
                                        "Work Done" -> Color(0xFF059669)
                                        "Work Done - Payment Pending" -> Color(0xFFDC2626)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        // Info rows with icons
                        DetailInfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Date Ordered",
                            value = currentEntry.dateString
                        )
                        DetailInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Mobile Number",
                            value = currentEntry.mobileNumber
                        )
                        DetailInfoRow(
                            icon = Icons.Default.Place,
                            label = "Location/Address",
                            value = currentEntry.locationAddress
                        )
                        DetailInfoRow(
                            icon = Icons.Default.Person,
                            label = "Reference Name",
                            value = currentEntry.referenceName.ifBlank { "Direct Referral / None" }
                        )
                    }
                }

                // Financial Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Financial Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        FinancialDetailItem(
                            label = "Total Work Amount",
                            value = viewModel.formatCurrency(currentEntry.amount),
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                        FinancialDetailItem(
                            label = "Material Cost",
                            value = viewModel.formatCurrency(currentEntry.materialCost),
                            valueColor = Color(0xFFEF4444) // Required RED
                        )
                        FinancialDetailItem(
                            label = "Labour Cost",
                            value = viewModel.formatCurrency(currentEntry.laborCost),
                            valueColor = Color(0xFFEF4444) // Required RED
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )

                        // Highlighted profit block
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (currentEntry.netProfit >= 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Net Profit",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentEntry.netProfit >= 0) Color(0xFF065F46) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = viewModel.formatCurrency(currentEntry.netProfit),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (currentEntry.netProfit >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                                )
                            }
                        }
                    }
                }

                // Invoice photo(s) card
                if (currentEntry.invoicePhotos.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Invoice Photos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                currentEntry.invoicePhotos.forEachIndexed { index, path ->
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline)
                                            .clickable { selectedPhotoForZoom = path }
                                    ) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Invoice Photo ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.05f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Notes Card
                if (currentEntry.notes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Notes / Remarks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentEntry.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quick Status Changer Row (Inline action)
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Quick Status Change",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Pending Work", "Work Done", "Work Done - Payment Pending").forEach { targetStatus ->
                                val isActive = currentEntry.status == targetStatus
                                val activeColor = when (targetStatus) {
                                    "Pending Work" -> Color(0xFFF59E0B)
                                    "Work Done" -> Color(0xFF10B981)
                                    "Work Done - Payment Pending" -> Color(0xFFEF4444)
                                    else -> MaterialTheme.colorScheme.primary
                                }

                                Button(
                                    onClick = {
                                        if (!isActive) {
                                            viewModel.updateEntry(currentEntry.copy(status = targetStatus))
                                            Toast.makeText(context, "Status updated to $targetStatus", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("status_quick_btn_${targetStatus.replace(" ", "_")}"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when (targetStatus) {
                                            "Pending Work" -> "Pending"
                                            "Work Done" -> "Completed"
                                            "Work Done - Payment Pending" -> "Unpaid"
                                            else -> targetStatus
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Row: Call, Share WhatsApp, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Call Customer
                    Button(
                        onClick = {
                            try {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentEntry.mobileNumber}"))
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("action_call_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call", fontWeight = FontWeight.Bold)
                    }

                    // Share WhatsApp
                    Button(
                        onClick = {
                            val cleanPhone = currentEntry.mobileNumber.filter { it.isDigit() }
                            val msg = """
                                *WorkFlow Job Update*
                                📋 Customer: ${currentEntry.customerName}
                                🛠️ Work: ${currentEntry.workType}
                                📅 Date: ${currentEntry.dateString}
                                📍 Location: ${currentEntry.locationAddress}
                                💳 Total Amount: ${viewModel.formatCurrency(currentEntry.amount)}
                                🔔 Status: ${currentEntry.status}
                            """.trimIndent()
                            try {
                                val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}"
                                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(waIntent)
                            } catch (e: Exception) {
                                // Fallback standard share sheet
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Order Details"))
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("action_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontWeight = FontWeight.Bold)
                    }

                    // Delete button (Red accent)
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("action_delete_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }

    // Zoom Dialog for photos
    selectedPhotoForZoom?.let { path ->
        Dialog(onDismissRequest = { selectedPhotoForZoom = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = File(path),
                    contentDescription = "Zoomed Invoice Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { selectedPhotoForZoom = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Work Order?") },
            text = { Text("Are you sure you want to delete this work order for ${entry?.customerName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        entry?.let {
                            viewModel.deleteEntry(it)
                            Toast.makeText(context, "Work order deleted!", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteDialog = false
                        onBack()
                    },
                    modifier = Modifier.testTag("delete_confirm_ok")
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, modifier = Modifier.testTag("delete_confirm_cancel")) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FinancialDetailItem(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
