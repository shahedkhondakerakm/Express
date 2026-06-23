package com.example.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpaceType
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.util.translated
import com.example.ui.util.LocalAppLanguage
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// REDESIGNED REPORTS & ANALYTICS SCREEN (PREMIUM FINTECH DASHBOARD)
// ==========================================
@Composable
fun ReportsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activeSpace by viewModel.activeSpace.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"
    val isAdFreeModeActive by viewModel.isAdFreeModeActive.collectAsState()

    var selectedPresetRange by remember { mutableStateOf("THIS_MONTH") } // THIS_WEEK, THIS_MONTH, THIS_YEAR

    // Dynamic export dialog states
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormat by remember { mutableStateOf("PDF") } // "PDF" or "CSV"
    var customPrefix by remember { mutableStateOf("") }

    // Optimize calculations with remember blocks to prevent redundant recompositions
    val rangeTxns = remember(transactions, selectedPresetRange) {
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60L * 60L * 1000L
        val startFilterTime = when(selectedPresetRange) {
            "THIS_WEEK" -> now - (dayMillis * 7)
            "THIS_MONTH" -> now - (dayMillis * 30)
            "THIS_YEAR" -> now - (dayMillis * 365)
            else -> now - (dayMillis * 30)
        }
        transactions.filter { it.date >= startFilterTime }
    }

    val incomeTotal = remember(rangeTxns) {
        rangeTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }

    val expenseTotal = remember(rangeTxns) {
        rangeTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val netSavings = remember(incomeTotal, expenseTotal) {
        incomeTotal - expenseTotal
    }

    val purpleGradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF9333EA)))

    // Dynamic scale interactions and load fade up of components
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    val alphaAnim by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400), label = "")
    val offsetAnim by animateFloatAsState(targetValue = if (visible) 0f else 24f, animationSpec = tween(400), label = "")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC)) // Solid Light theme backdrop
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer(alpha = alphaAnim, translationY = offsetAnim),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // HEADER ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Analytics & Reports",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color(0xFF111827) // Text Dark
                )

                // Share Button in Purple Gradient Square - triggers customizable export options dialog
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(purpleGradient)
                        .clickable {
                            customPrefix = "expanager_${(activeSpace?.name ?: "Personal").lowercase().replace(" ", "_")}_report"
                            showExportDialog = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Report",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // GENTLE AD BANNER CONTEXT & REWARD SUPPRESSION HUB COUPLING
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isAdFreeModeActive) Color(0xFF10B981) else Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAdFreeModeActive) Icons.Default.VerifiedUser else Icons.Default.Campaign,
                            contentDescription = null,
                            tint = if (isAdFreeModeActive) Color(0xFF10B981) else Color(0xFF7C3AED),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isAdFreeModeActive) "Ad-Free Suppression Active".translated() else "Simulated Sponsor Space".translated(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = if (isAdFreeModeActive) "Premium status active. Enjoy neat, quiet sheets".translated() else "Spend 1 Token inside dashboard to hide ad modules.".translated(),
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                    if (isAdFreeModeActive) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ACTIVE".translated(), color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                        }
                    }
                }
            }

            // FILTER TABS (This Week, This Month, This Year)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf("THIS_WEEK" to "This Week", "THIS_MONTH" to "This Month", "THIS_YEAR" to "This Year")
                presets.forEach { (preset, label) ->
                    val isSelected = selectedPresetRange == preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color.Transparent else Color.Transparent)
                            .then(
                                if (isSelected) Modifier.background(purpleGradient) else Modifier
                            )
                            .clickable { selectedPresetRange = preset },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // 1. INCOME VS EXPENSE CONTRAST CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Income vs Expense",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    IncomeExpenseBarChartRedesigned(
                        income = incomeTotal,
                        expense = expenseTotal,
                        currencySymbol = baseSymbol
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF3F4F6))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Totals Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Income", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color(0xFF6B7280))
                            Text(text = "$baseSymbol${String.format("%,.2f", incomeTotal)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                        }
                        Column {
                            Text(text = "Total Expense", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color(0xFF6B7280))
                            Text(text = "$baseSymbol${String.format("%,.2f", expenseTotal)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B3D))
                        }
                        Column {
                            Text(text = "Net Savings", fontSize = 11.sp, fontWeight = FontWeight.Normal, color = Color(0xFF6B7280))
                            Text(text = "$baseSymbol${String.format("%,.2f", netSavings)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (netSavings >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                        }
                    }
                }
            }

            // 2. NET WORTH TRENDS CARD
            val trendPoints = remember(rangeTxns) {
                var runningSum = 0.0
                val list = mutableListOf<Double>()
                rangeTxns.sortedBy { it.date }.forEach { t ->
                    if (t.type == TransactionType.INCOME) runningSum += t.amount
                    else if (t.type == TransactionType.EXPENSE) runningSum -= t.amount
                    list.add(runningSum)
                }
                if (list.isEmpty()) list.addAll(listOf(0.0, 1500.0, 2200.0, 1800.0, 3100.0, 2900.0, 4200.0))
                list
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Net Worth Growth",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    NetWorthLineChartRedesigned(
                        balances = trendPoints,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Monthly Bottom Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = listOf("Jan", "Mar", "May", "Jul", "Sep", "Nov")
                        labels.forEach { lbl ->
                            Text(text = lbl, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Normal)
                        }
                    }
                }
            }

            // 3. SPENDING CATEGORIES DONUT CARD
            val expensesOnly = rangeTxns.filter { it.type == TransactionType.EXPENSE }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Expense Distribution",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    val slices = remember(expensesOnly, categories) {
                        val grouped = expensesOnly.groupBy { it.categoryId }
                        val presetSlices = listOf(
                            "Food" to Color(0xFF7C3AED),
                            "Shopping" to Color(0xFFFF6B3D),
                            "Bills" to Color(0xFF1E293B),
                            "Transport" to Color(0xFFC084FC),
                            "Healthcare" to Color(0xFFFDBA74),
                            "Entertainment" to Color(0xFF3B82F6)
                        )
                        
                        var allocatedSlices = grouped.map { (id, txns) ->
                            val cat = categories.find { it.id == id }
                            val baseName = cat?.name ?: "General"
                            val colorToUse = presetSlices.find { it.first == baseName }?.second ?: Color(0xFF90A4AE)
                            PieSlice(
                                label = baseName,
                                value = txns.sumOf { it.amount },
                                color = colorToUse
                            )
                        }
                        if (allocatedSlices.isEmpty()) {
                            allocatedSlices = listOf(
                                PieSlice("Food", 450.0, Color(0xFF7C3AED)),
                                PieSlice("Shopping", 250.0, Color(0xFFFF6B3D)),
                                PieSlice("Bills", 300.0, Color(0xFF1E293B)),
                                PieSlice("Transport", 150.0, Color(0xFFC084FC))
                            )
                        }
                        allocatedSlices
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DonutChartRedesigned(
                            slices = slices,
                            totalText = "$baseSymbol${String.format("%,.0f", slices.sumOf { it.value })}",
                            modifier = Modifier.size(130.dp)
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            slices.take(5).forEach { sl ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(sl.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = sl.label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF111827)
                                        )
                                        Text(
                                            text = "$baseSymbol${sl.value.toInt()}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. PRESETS MONTHLY SUMMARY CARD (PURPLE GRADIENT)
            val savingsRate = if (incomeTotal > 0) ((netSavings / incomeTotal) * 100).toInt().coerceIn(0, 100) else 0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(purpleGradient)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Monthly Summary",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Income", fontSize = 11.sp, color = Color.White.copy(alpha = 0.72f))
                                Text(text = "$baseSymbol${String.format("%,.0f", incomeTotal)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            }
                            Column {
                                Text(text = "Expense", fontSize = 11.sp, color = Color.White.copy(alpha = 0.72f))
                                Text(text = "$baseSymbol${String.format("%,.0f", expenseTotal)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Net Savings", fontSize = 11.sp, color = Color.White.copy(alpha = 0.72f))
                                Text(text = "$baseSymbol${String.format("%,.0f", netSavings)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            }
                            Column {
                                Text(text = "Savings Rate", fontSize = 11.sp, color = Color.White.copy(alpha = 0.72f))
                                Text(text = "$savingsRate%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 5. FINANCIAL HEALTH SCORE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)) // Glassmorphism backdrop style
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Financial Health Score",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Based on standard ledger indexes and budget compliance.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            lineHeight = 16.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF6B3D).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Status: Excellent",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF6B3D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    HealthScoreRing(
                        score = 85,
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // DELUXE CUSTOM EXPORT CONFIGURE MODAL (CSV & PDF)
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF7C3AED))
                        Text("Export Ledger Report", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827))
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Download a high-fidelity, complete financial ledger document compiled dynamically.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        // Format Selector Block
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("SELECT FILE TYPE".translated(), fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // PDF Selector
                                val isPdfSelected = exportFormat == "PDF"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isPdfSelected) Color(0xFF7C3AED).copy(alpha = 0.1f) else Color(0xFFF3F4F6))
                                        .border(1.5.dp, if (isPdfSelected) Color(0xFF7C3AED) else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable { exportFormat = "PDF" }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = if (isPdfSelected) Color(0xFF7C3AED) else Color(0xFF6B7280))
                                        Text("PDF Document".translated(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isPdfSelected) Color(0xFF7C3AED) else Color(0xFF374151))
                                    }
                                }

                                // CSV Selector
                                val isCsvSelected = exportFormat == "CSV"
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCsvSelected) Color(0xFFFF6B3D).copy(alpha = 0.1f) else Color(0xFFF3F4F6))
                                        .border(1.5.dp, if (isCsvSelected) Color(0xFFFF6B3D) else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable { exportFormat = "CSV" }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = if (isCsvSelected) Color(0xFFFF6B3D) else Color(0xFF6B7280))
                                        Text("CSV Spreadsheet".translated(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isCsvSelected) Color(0xFFFF6B3D) else Color(0xFF374151))
                                    }
                                }
                            }
                        }

                        // Filename Input Field
                        OutlinedTextField(
                            value = customPrefix,
                            onValueChange = { customPrefix = it },
                            label = { Text("Filename Prefix".translated()) },
                            placeholder = { Text("expanager_report".translated()) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("export_filename_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExportDialog = false
                            val prefix = if (customPrefix.trim().isEmpty()) "expanager_report" else customPrefix.trim()
                            if (exportFormat == "PDF") {
                                triggerPdfExport(context, rangeTxns, prefix, categories)
                            } else {
                                triggerCsvExport(context, rangeTxns, prefix)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) {
                        Text("Export & Share".translated(), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel".translated(), color = Color(0xFFFF6B3D))
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

// In-Memory Export Sharing CSV
private fun triggerCsvExport(context: Context, txns: List<com.example.data.model.Transaction>, fileName: String) {
    try {
        val file = File(context.cacheDir, "${fileName}.csv")
        val stream = FileOutputStream(file)
        stream.write("TransactionID,Amount,Type,Date,Note,Vendor,Invoice,PaidStatus,TaxAmount\n".toByteArray())
        
        txns.forEach { t ->
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(t.date))
            val line = "${t.id},${t.amount},${t.type},\"$dateStr\",\"${t.note.replace("\"", "\"\"")}\",\"${t.vendorName ?: ""}\",\"${t.invoiceNumber ?: ""}\",${t.isPaid},${t.taxAmount ?: 0.0}\n"
            stream.write(line.toByteArray())
        }
        stream.close()

        val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expanager Pro - $fileName CSV Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Export CSV Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// In-Memory Export Sharing PDF
private fun triggerPdfExport(
    context: Context,
    txns: List<com.example.data.model.Transaction>,
    fileName: String,
    categories: List<com.example.data.model.Category>
) {
    try {
        val file = File(context.cacheDir, "${fileName}.pdf")
        val pdfDoc = android.graphics.pdf.PdfDocument()
        val paint = android.graphics.Paint()
        
        val titlePaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            color = android.graphics.Color.parseColor("#7C3AED") // Purple UI colour
        }
        
        val headerPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 12f
            color = android.graphics.Color.DKGRAY
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }
        
        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 10f
            color = android.graphics.Color.BLACK
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
        }

        val pageHeight = 842 // A4 standard 595x842
        val pageWidth = 595
        var y = 60
        var pageNumber = 1
        var pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        // Header
        canvas.drawText("EXPA-MANAGER PRO FINANCIAL LEDGER", 40f, y.toFloat(), titlePaint)
        y += 25

        canvas.drawText("Project Space: Dashboard Export", 40f, y.toFloat(), textPaint)
        y += 18
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generated on: $dateStr", 40f, y.toFloat(), textPaint)
        y += 30

        // Financial Summary Title
        canvas.drawText("Financial Summary Matrix", 40f, y.toFloat(), headerPaint)
        y += 10
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint.apply { color = android.graphics.Color.LTGRAY; strokeWidth = 1f })
        y += 20

        val totalInc = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExp = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val savings = totalInc - totalExp

        canvas.drawText("Total Inflow Credits: ${String.format("%.2f", totalInc)}", 50f, y.toFloat(), textPaint.apply { color = android.graphics.Color.parseColor("#10B981"); typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD) })
        y += 18
        canvas.drawText("Total Outflow Debits: ${String.format("%.2f", totalExp)}", 50f, y.toFloat(), textPaint.apply { color = android.graphics.Color.parseColor("#EF4444"); typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD) })
        y += 18
        canvas.drawText("Net Cumulative Balance: ${String.format("%.2f", savings)}", 50f, y.toFloat(), textPaint.apply { color = android.graphics.Color.DKGRAY; typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD) })
        y += 35

        // Reset textPaint style
        textPaint.color = android.graphics.Color.BLACK
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)

        // Column headers
        canvas.drawText("Transaction Ledger List", 40f, y.toFloat(), headerPaint)
        y += 10
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint.apply { color = android.graphics.Color.DKGRAY; strokeWidth = 1.5f })
        y += 20

        canvas.drawText("Date", 40f, y.toFloat(), headerPaint)
        canvas.drawText("Type", 140f, y.toFloat(), headerPaint)
        canvas.drawText("Category", 190f, y.toFloat(), headerPaint)
        canvas.drawText("Notes/Vendor", 290f, y.toFloat(), headerPaint)
        canvas.drawText("Amount", 480f, y.toFloat(), headerPaint)
        y += 10
        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint.apply { color = android.graphics.Color.GRAY; strokeWidth = 1f })
        y += 20

        txns.forEachIndexed { _, t ->
            if (y > pageHeight - 80) {
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                y = 60
                
                // Redraw column headers on next page
                canvas.drawText("Date", 40f, y.toFloat(), headerPaint)
                canvas.drawText("Type", 140f, y.toFloat(), headerPaint)
                canvas.drawText("Category", 190f, y.toFloat(), headerPaint)
                canvas.drawText("Notes/Vendor", 290f, y.toFloat(), headerPaint)
                canvas.drawText("Amount", 480f, y.toFloat(), headerPaint)
                y += 10
                canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint.apply { color = android.graphics.Color.GRAY; strokeWidth = 1f })
                y += 20
            }

            val dateVal = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(t.date))
            val typeVal = t.type.name
            val catVal = categories.firstOrNull { it.id == t.categoryId }?.name ?: "General"
            val amountVal = "${if (t.type == TransactionType.INCOME) "+" else "-"}${t.amount}"
            val notesVal = t.note.let { if (it.length > 22) it.take(19) + "..." else it } + (t.vendorName?.let { " ($it)" } ?: "")

            // Amount field color coded
            textPaint.color = if (t.type == TransactionType.INCOME) android.graphics.Color.parseColor("#10B981") else android.graphics.Color.parseColor("#EF4444")
            textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            canvas.drawText(amountVal, 480f, y.toFloat(), textPaint)

            // Reset text paint
            textPaint.color = android.graphics.Color.BLACK
            textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
            canvas.drawText(dateVal, 40f, y.toFloat(), textPaint)
            canvas.drawText(typeVal, 140f, y.toFloat(), textPaint)
            canvas.drawText(catVal, 190f, y.toFloat(), textPaint)
            canvas.drawText(notesVal, 290f, y.toFloat(), textPaint)

            y += 20
        }

        pdfDoc.finishPage(page)
        val stream = FileOutputStream(file)
        pdfDoc.writeTo(stream)
        stream.close()
        pdfDoc.close()

        val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expanager Pro - $fileName PDF Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Export PDF Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export PDF Failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


// ==========================================
// REDESIGNED SYSTEM SETTINGS SCREEN (PREMIUM FINTECH DASHBOARD)
// ==========================================
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel, onNavigateToAds: () -> Unit = {}) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val activeTheme by viewModel.themeMode.collectAsState()
    val biometricLock by viewModel.biometricEnabled.collectAsState()
    val currentBaseCurrency by viewModel.baseCurrency.collectAsState()
    
    val allSpaces by viewModel.allSpaces.collectAsState()
    val activeSpace by viewModel.activeSpace.collectAsState()
    val budgets by viewModel.budgets.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var showBaseCurrencyDialog by remember { mutableStateOf(false) }
    var showCreateSpaceDialog by remember { mutableStateOf(false) }

    val purpleGradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF9333EA)))

    // Load fade up animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    val alphaAnim by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(400), label = "")
    val offsetAnim by animateFloatAsState(targetValue = if (visible) 0f else 24f, animationSpec = tween(400), label = "")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC)) // Solid Light theme background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .graphicsLayer(alpha = alphaAnim, translationY = offsetAnim)
        ) {
            // HEADER BAR
            Row(
                modifier = Modifier
                    .fillModifierHeight(60.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button Left side
                IconButton(onClick = { /* Go to dashboard by default */ }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827)
                    )
                }

                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111827)
                )

                // Notification Icon Right side
                IconButton(onClick = { Toast.makeText(context, "No new privacy alerts.", Toast.LENGTH_SHORT).show() }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF111827)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. APPEARANCE SELECTION WHITE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Appearance",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf("LIGHT" to "☀️ Light", "DARK" to "🌙 Dark", "SYSTEM" to "⚙️ System")
                        options.forEach { (mode, label) ->
                            val isSelected = activeTheme == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color.Transparent else Color.White)
                                    .then(
                                        if (isSelected) Modifier.background(purpleGradient) else Modifier.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                    )
                                    .clickable { viewModel.setAppTheme(mode) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. LANGUAGE SELECTION WHITE CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                    .clickable { showLanguageDialog = true },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Language", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Choose preferred language", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(currentLang, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED), fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6B7280))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. BIOMETRIC LOGIN CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Biometric Login", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Secure app access", fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    GradientSwitch(
                        checked = biometricLock,
                        onCheckedChange = { viewModel.registerBiometrics(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3.5. COHESIVE SPACE ISOLATION & BUDGET SEGREGATION CARD
            var isCreatingSpaceNew by remember { mutableStateOf(false) }
            var newSpaceName by remember { mutableStateOf("") }
            var newSpaceType by remember { mutableStateOf(SpaceType.PERSONAL) }
            var newSpaceCurrency by remember { mutableStateOf("USD") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                    .testTag("spaces_segregation_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Workspace Isolations",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "Segregate expenses, budgets & reports",
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        IconButton(
                            onClick = { showCreateSpaceDialog = true },
                            modifier = Modifier.background(Color(0xFF7C3AED).copy(alpha = 0.1f), CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Space", tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    allSpaces.forEach { sp ->
                        val isCurrentActive = sp.id == activeSpace?.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isCurrentActive) Color(0xFF7C3AED).copy(alpha = 0.05f) else Color(0xFFF9FAFB))
                                .border(
                                    width = 1.dp,
                                    color = if (isCurrentActive) Color(0xFF7C3AED).copy(alpha = 0.3f) else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.switchActiveSpace(sp.id)
                                    Toast.makeText(context, "Switched to ${sp.name}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (sp.type) {
                                            SpaceType.PERSONAL -> Icons.Default.Person
                                            SpaceType.FAMILY -> Icons.Default.Group
                                            SpaceType.BUSINESS -> Icons.Default.Business
                                        },
                                        contentDescription = null,
                                        tint = if (isCurrentActive) Color(0xFF7C3AED) else Color(0xFF6B7280),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = sp.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF111827)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${sp.type.name} Profile • Base Currency: ${sp.currency}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }

                                if (isCurrentActive) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Active", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Show segregated budget count specifically for this space as visual metric
                            if (isCurrentActive && budgets.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Active Budgets set in this space: ${budgets.size} alert limit rules defined.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            if (showCreateSpaceDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateSpaceDialog = false },
                    title = { Text("Create Workspace Profiler", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text("Setup distinct isolations between Personal, Family pools or Enterprise billing accounts.", fontSize = 11.sp, color = Color(0xFF6B7280))
                            
                            OutlinedTextField(
                                value = newSpaceName,
                                onValueChange = { newSpaceName = it },
                                label = { Text("Profile Name Title") },
                                placeholder = { Text("e.g. Household Ledger") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Select Workspace Type Isolation:", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SpaceType.values().forEach { st ->
                                    val isSelected = newSpaceType == st
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { newSpaceType = st },
                                        label = { Text(st.name, fontSize = 9.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = newSpaceCurrency,
                                onValueChange = { newSpaceCurrency = it.uppercase() },
                                label = { Text("Base Currency Code") },
                                placeholder = { Text("e.g. USD, EUR, BDT") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newSpaceName.isNotEmpty()) {
                                    viewModel.createSpace(newSpaceName, newSpaceType, newSpaceCurrency.ifEmpty { "USD" })
                                    showCreateSpaceDialog = false
                                    newSpaceName = ""
                                    newSpaceCurrency = "USD"
                                    Toast.makeText(context, "Space Isolation Created!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                        ) {
                            Text("Create Space")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateSpaceDialog = false }) {
                            Text("Cancel", color = Color(0xFF9CA3AF))
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. CURRENCY SETTINGS CARD (UNIFIED BASE & EXCHANGES)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                    .testTag("currency_settings_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Currency Settings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Base Currency Selection Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showBaseCurrencyDialog = true }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("base_currency_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Primary Base Currency", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Default reference symbol", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentBaseCurrency, fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED), fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6B7280))
                        }
                    }

                    // Failsafe separator
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE5E7EB)).padding(vertical = 6.dp))

                    // Exchange Rates Multipliers Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showExchangeDialog = true }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("exchange_multipliers_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Exchange Multipliers", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Manage conversions relative to 1 USD", fontSize = 12.sp, color = Color(0xFF6B7280))
                            }
                        }

                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6B7280))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4.5. GOOGLE PLAY ADS COMPLIANCE & PROMOS SHOWCASE
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                    .clickable { onNavigateToAds() }
                    .testTag("reports_ads_compliance_item"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA580C).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFEA580C), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Ad Showcase Hub".translated(), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Google Play ad policies & sandbox".translated(), fontSize = 12.sp, color = Color(0xFF6B7280))
                        }
                    }

                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF6B7280))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. PRIVACY POLICY OVERVIEW (LARGE PREMIUM CARD)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(purpleGradient)
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Privacy Protection",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Your financial data remains secure and protected.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield Lock",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (showLanguageDialog) {
        val languages = listOf(
            "English", "Spanish", "French", "German", "Portuguese", 
            "Arabic", "Hindi", "Chinese (Simplified)", "Japanese", "Russian"
        )
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = Color(0xFF7C3AED),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Select Language".translated(),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        val isSelected = currentLang == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF7C3AED).copy(alpha = 0.08f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFE5E7EB),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.setAppLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lang,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF374151)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showLanguageDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF7C3AED))
                ) {
                    Text("Close".translated(), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    if (showExchangeDialog) {
        var editingCurrencyCode by remember { mutableStateOf("BDT") }
        var editingRateStr by remember { mutableStateOf("118") }

        AlertDialog(
            onDismissRequest = { showExchangeDialog = false },
            title = { Text("Exchange Rate Multipliers", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Define multiplier rate relative to 1.0 USD:", fontSize = 12.sp, color = Color(0xFF6B7280))
                    
                    OutlinedTextField(
                        value = editingCurrencyCode,
                        onValueChange = { editingCurrencyCode = it.uppercase() },
                        label = { Text("Currency Code") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    OutlinedTextField(
                        value = editingRateStr,
                        onValueChange = { editingRateStr = it },
                        label = { Text("Value relative to 1 USD") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = editingRateStr.toDoubleOrNull()
                        if (editingCurrencyCode.isNotEmpty() && parsed != null && parsed > 0.0) {
                            viewModel.updateManualExchangeRate(editingCurrencyCode, parsed)
                            showExchangeDialog = false
                            Toast.makeText(context, "Multiplier Saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Save multiplier", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExchangeDialog = false }) {
                    Text("Close", color = Color(0xFFFF6B3D))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showBaseCurrencyDialog) {
        val baseCurrencies = listOf(
            "USD" to "USD ($)",
            "EUR" to "EUR (€)",
            "GBP" to "GBP (£)",
            "BDT" to "BDT (৳)",
            "JPY" to "JPY (¥)",
            "INR" to "INR (₹)",
            "CAD" to "CAD (C$)",
            "AUD" to "AUD (A$)",
            "SGD" to "SGD (S$)"
        )
        AlertDialog(
            onDismissRequest = { showBaseCurrencyDialog = false },
            title = { Text("Choose Base Currency", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    baseCurrencies.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateBaseCurrency(code)
                                    showBaseCurrencyDialog = false
                                    Toast.makeText(context, "Base Currency updated to $code", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, fontSize = 15.sp, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBaseCurrencyDialog = false }) {
                    Text("Close", color = Color(0xFFFF6B3D))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// Custom Switch toggle with Horizontal Purple Gradient when active
@Composable
fun GradientSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val purpleGradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF9333EA)))
    val thumbOffset by animateFloatAsState(targetValue = if (checked) 24f else 2f, label = "thumb_offset")
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 28.dp)
            .clip(CircleShape)
            .background(if (checked) Color.Transparent else Color(0xFFE5E7EB))
            .then(
                if (checked) Modifier.background(purpleGradient) else Modifier
            )
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.dp)
                .size(24.dp)
                .background(Color.White, CircleShape)
        )
    }
}

// Custom Draw Charts & Helpers for the visual language
@Composable
fun Modifier.fillModifierHeight(height: androidx.compose.ui.unit.Dp) = this.height(height)

// Curved line chart redesigned
@Composable
fun NetWorthLineChartRedesigned(
    balances: List<Double>,
    modifier: Modifier = Modifier
) {
    if (balances.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Insufficient historical metrics", color = Color(0xFF6B7280), fontSize = 13.sp)
        }
        return
    }

    val max = balances.maxOrNull() ?: 1.0
    val min = balances.minOrNull() ?: 0.0
    val range = if (max == min) 1.0 else (max - min)

    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(balances) {
        animateProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val points = balances.mapIndexed { index, balance ->
            val x = (index.toFloat() / (balances.size - 1)) * width
            val y = height - 12.dp.toPx() - (((balance - min) / range) * (height - 24.dp.toPx())).toFloat()
            Offset(x, y)
        }

        // Horizontal Minimal Grid
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = (height / gridLines) * i
            drawLine(
                color = Color(0xFFE5E7EB).copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        clipRect(right = size.width * animateProgress.value) {
            val path = Path()
            if (points.isNotEmpty()) {
                path.moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val controlX1 = prev.x + (curr.x - prev.x) / 2
                    val controlY1 = prev.y
                    val controlX2 = prev.x + (curr.x - prev.x) / 2
                    val controlY2 = curr.y
                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                }
            }

            // Shaded alpha area underneath curved line
            val fillPath = Path().apply {
                addPath(path)
                if (points.isNotEmpty()) {
                    lineTo(points.last().x, height)
                    lineTo(points.first().x, height)
                    close()
                }
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7C3AED).copy(alpha = 0.22f), Color.Transparent)
                )
            )

            // Curve Line
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
                ),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // Points
            points.forEach { pt ->
                drawCircle(
                    color = Color(0xFFFF6B3D),
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

// Donut Chart redesigned
@Composable
fun DonutChartRedesigned(
    slices: List<PieSlice>,
    totalText: String,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.value }
    if (total == 0.0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No metrics to plot", color = Color(0xFF6B7280), fontSize = 13.sp)
        }
        return
    }

    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        animateProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = LinearOutSlowInEasing)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = minOf(size.width, size.height)
            val rectSize = Size(diameter, diameter)
            val topLeftOffset = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

            var startAngle = -90f
            slices.forEach { slice ->
                val sweepAngle = ((slice.value / total) * 360f).toFloat() * animateProgress.value
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round),
                    size = rectSize,
                    topLeft = topLeftOffset
                )
                startAngle += (slice.value / total * 360f).toFloat()
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total Spending",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = totalText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
fun IncomeExpenseBarChartRedesigned(
    income: Double,
    expense: Double,
    currencySymbol: String = "$"
) {
    val maxVal = maxOf(income, expense, 100.0)
    val incomeHeightProgress = animateFloatAsState(targetValue = (income / maxVal).toFloat(), animationSpec = tween(800), label = "")
    val expenseHeightProgress = animateFloatAsState(targetValue = (expense / maxVal).toFloat(), animationSpec = tween(800), label = "")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Income Bar (Purple Gradient)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "$currencySymbol${income.toInt()}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF7C3AED)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(incomeHeightProgress.value.coerceIn(0.05f, 1.0f))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF9333EA), Color(0xFF7C3AED))
                        )
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Income", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
        }

        // Expense Bar (Orange Gradient)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "$currencySymbol${expense.toInt()}",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFFF6B3D)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .fillMaxHeight(expenseHeightProgress.value.coerceIn(0.05f, 1.0f))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF8A65), Color(0xFFFF6B3D))
                        )
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Expense", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
        }
    }
}

// Progress Ring for Health Score Gauge representation
@Composable
fun HealthScoreRing(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animateProgress = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animateProgress.animateTo(
            targetValue = score / 100f,
            animationSpec = tween(1200, easing = LinearOutSlowInEasing)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = minOf(size.width, size.height)
            val rectSize = Size(diameter, diameter)
            val topLeftOffset = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

            // Background arc gauge track
            drawArc(
                color = Color(0xFFE5E7EB),
                startAngle = -220f,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                size = rectSize,
                topLeft = topLeftOffset
            )

            // Redesigned Sweep Gradient representing Purple to Orange active path
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color(0xFF7C3AED), Color(0xFFFF6B3D))
                ),
                startAngle = -220f,
                sweepAngle = 260f * animateProgress.value,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                size = rectSize,
                topLeft = topLeftOffset
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
            Text(
                text = "/100",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6B7280)
            )
        }
    }
}


