package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.WorkEntry
import com.example.viewmodel.WorkViewModel
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: WorkViewModel,
    entryId: Int?, // null if adding new
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Form states
    var customerName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var referenceName by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var materialText by remember { mutableStateOf("") }
    var laborText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending Work") }
    var attachedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }

    var dateTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var dateString by remember { mutableStateOf(viewModel.formatDate(System.currentTimeMillis())) }

    var isEditMode by remember { mutableStateOf(false) }
    var existingEntry by remember { mutableStateOf<WorkEntry?>(null) }

    // Dropdown state for Status
    var statusMenuExpanded by remember { mutableStateOf(false) }

    // Load existing entry if in Edit Mode
    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.getEntryById(entryId) { entry ->
                if (entry != null) {
                    existingEntry = entry
                    isEditMode = true
                    customerName = entry.customerName
                    mobileNumber = entry.mobileNumber
                    locationAddress = entry.locationAddress
                    referenceName = entry.referenceName
                    workType = entry.workType
                    amountText = entry.amount.toString()
                    materialText = entry.materialCost.toString()
                    laborText = entry.laborCost.toString()
                    notes = entry.notes
                    status = entry.status
                    attachedPhotos = entry.invoicePhotos
                    dateTimestamp = entry.dateTimestamp
                    dateString = entry.dateString
                }
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val localPath = viewModel.saveUriToInternalStorage(context, it)
                if (localPath != null) {
                    attachedPhotos = attachedPhotos + localPath
                } else {
                    Toast.makeText(context, "Failed to load photo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Real-time calculations for Net Profit
    val amountVal = amountText.toDoubleOrNull() ?: 0.0
    val materialVal = materialText.toDoubleOrNull() ?: 0.0
    val laborVal = laborText.toDoubleOrNull() ?: 0.0
    val liveNetProfit = amountVal - materialVal - laborVal

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Work Order" else "New Work Order",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("add_edit_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Customer & Job Info
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Job Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Customer Name
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_customer_name"),
                        singleLine = true
                    )

                    // Mobile Number
                    OutlinedTextField(
                        value = mobileNumber,
                        onValueChange = { mobileNumber = it },
                        label = { Text("Mobile Number *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_mobile_number"),
                        singleLine = true
                    )

                    // Location / Address
                    OutlinedTextField(
                        value = locationAddress,
                        onValueChange = { locationAddress = it },
                        label = { Text("Location/Address *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_location_address"),
                        singleLine = true
                    )

                    // Reference Name
                    OutlinedTextField(
                        value = referenceName,
                        onValueChange = { referenceName = it },
                        label = { Text("Reference Name (Who referred)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_reference_name"),
                        singleLine = true
                    )

                    // Date Picker Trigger
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = {},
                        label = { Text("Date *") },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                modifier = Modifier
                                    .clickable {
                                        val cal = Calendar.getInstance()
                                        cal.timeInMillis = dateTimestamp
                                        DatePickerDialog(
                                            context,
                                            { _, year, month, day ->
                                                cal.set(year, month, day, 0, 0, 0)
                                                dateTimestamp = cal.timeInMillis
                                                dateString = viewModel.formatDate(cal.timeInMillis)
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        ).show()
                                    }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_date_field")
                    )

                    // Work Type (Manual free text)
                    OutlinedTextField(
                        value = workType,
                        onValueChange = { workType = it },
                        label = { Text("Work Type *") },
                        placeholder = { Text("Enter manual work type...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_work_type"),
                        singleLine = true
                    )
                }
            }

            // Section 2: Finances
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Financial Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Amount Billed
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Billed Amount * (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_amount_billed"),
                        singleLine = true
                    )

                    // Material Cost
                    OutlinedTextField(
                        value = materialText,
                        onValueChange = { materialText = it },
                        label = { Text("Material Cost * (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_material_cost"),
                        singleLine = true
                    )

                    // Labor Cost
                    OutlinedTextField(
                        value = laborText,
                        onValueChange = { laborText = it },
                        label = { Text("Labor Cost * (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_labor_cost"),
                        singleLine = true
                    )

                    // Net Profit Display (Dynamic real-time box)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (liveNetProfit >= 0) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Net Profit",
                                fontWeight = FontWeight.Bold,
                                color = if (liveNetProfit >= 0) Color(0xFF065F46) else Color(0xFF991B1B)
                            )
                            Text(
                                text = viewModel.formatCurrency(liveNetProfit),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (liveNetProfit >= 0) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                        }
                    }
                }
            }

            // Section 3: Status & Invoice Photos & Remarks
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Work Status & Media",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Status Dropdown selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { statusMenuExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_status_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Status: $status")
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        ) {
                            listOf("Pending Work", "Work Done", "Work Done - Payment Pending").forEach { item ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = item,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        status = item
                                        statusMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Invoice Photo(s) Header / Attachment
                    Text(
                        text = "Invoice Photo(s)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Photo Thumbnails Row
                    if (attachedPhotos.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            attachedPhotos.forEachIndexed { index, path ->
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = "Invoice $index",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Remove overlay
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable {
                                                attachedPhotos = attachedPhotos.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Button to add photos
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_add_photo_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Invoice Photo")
                    }

                    // Notes / Remarks
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Remarks (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("form_notes"),
                        maxLines = 4
                    )
                }
            }

            // Form Submit Button
            Button(
                onClick = {
                    // Validations
                    if (customerName.isBlank() || mobileNumber.isBlank() || locationAddress.isBlank() || workType.isBlank() || amountText.isBlank() || materialText.isBlank() || laborText.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields (*)!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val finalAmount = amountText.toDoubleOrNull()
                    val finalMaterial = materialText.toDoubleOrNull()
                    val finalLabor = laborText.toDoubleOrNull()

                    if (finalAmount == null || finalMaterial == null || finalLabor == null) {
                        Toast.makeText(context, "Please enter valid numbers for finances!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val entry = WorkEntry(
                        id = entryId ?: 0,
                        customerName = customerName.trim(),
                        mobileNumber = mobileNumber.trim(),
                        locationAddress = locationAddress.trim(),
                        referenceName = referenceName.trim(),
                        workType = workType.trim(),
                        amount = finalAmount,
                        materialCost = finalMaterial,
                        laborCost = finalLabor,
                        notes = notes.trim(),
                        status = status,
                        invoicePhotos = attachedPhotos,
                        dateTimestamp = dateTimestamp,
                        dateString = dateString
                    )

                    if (isEditMode) {
                        viewModel.updateEntry(entry)
                        Toast.makeText(context, "Work order updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.insertEntry(entry)
                        Toast.makeText(context, "Work order created successfully!", Toast.LENGTH_SHORT).show()
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("form_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Save Changes" else "Create Work Order",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
