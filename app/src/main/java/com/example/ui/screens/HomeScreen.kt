package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WorkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WorkViewModel,
    modifier: Modifier = Modifier
) {
    val totalWorkAmount by viewModel.totalWorkAmount.collectAsState()
    val materialCost by viewModel.materialCost.collectAsState()
    val laborCost by viewModel.laborCost.collectAsState()
    val pendingAmount by viewModel.pendingAmount.collectAsState()
    val profitAmount by viewModel.profitAmount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WorkFlow Dashboard",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Total Work Amount
            SummaryCard(
                title = "Total Work Amount",
                value = viewModel.formatCurrency(totalWorkAmount),
                icon = Icons.Default.Payments,
                valueColor = MaterialTheme.colorScheme.onSurface,
                testTag = "summary_total_work_amount"
            )

            // Card 2: Material Cost - RED
            SummaryCard(
                title = "Material Cost",
                value = viewModel.formatCurrency(materialCost),
                icon = Icons.Default.Construction,
                valueColor = Color(0xFFEF4444), // Crimson/Red
                testTag = "summary_material_cost"
            )

            // Card 3: Labour Cost - RED
            SummaryCard(
                title = "Labour Cost",
                value = viewModel.formatCurrency(laborCost),
                icon = Icons.Default.Engineering,
                valueColor = Color(0xFFEF4444), // Crimson/Red
                testTag = "summary_labour_cost"
            )

            // Card 4: Pending Amount - ORANGE
            SummaryCard(
                title = "Pending Amount",
                value = viewModel.formatCurrency(pendingAmount),
                icon = Icons.Default.HourglassEmpty,
                valueColor = Color(0xFFFF7A00), // Accent Orange
                testTag = "summary_pending_amount"
            )

            // Card 5: Profit Amount - GREEN
            SummaryCard(
                title = "Profit Amount",
                value = viewModel.formatCurrency(profitAmount),
                icon = Icons.Default.TrendingUp,
                valueColor = Color(0xFF10B981), // Green
                testTag = "summary_profit_amount"
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: ImageVector,
    valueColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = valueColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = valueColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
