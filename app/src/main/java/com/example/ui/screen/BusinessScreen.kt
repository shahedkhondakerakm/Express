package com.example.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.data.model.*
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.InvoiceItemDraft
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun BusinessScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val activeSpace by viewModel.activeSpace.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val salaryPayments by viewModel.salaryPayments.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val fuelLogs by viewModel.fuelLogs.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"

    var activeSubTab by remember { mutableStateOf(0) } // 0=Payroll, 1=Payables, 2=Fuel, 3=Invoices(AR), 4=Reconciler

    if (activeSpace?.type != SpaceType.BUSINESS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select a Business Space", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Activate a Business Workspace from the pool bar selector dropdown to unlock salary sheets, AP/AR auditing ledger, fuel distance metrics, and bank reconciliation checklists.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("${activeSpace?.name} Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Scrollable SubTab Selector bar
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = activeSubTab == 0, onClick = { activeSubTab = 0 }) {
                Text("Payroll", Modifier.padding(10.dp), fontSize = 12.sp)
            }
            Tab(selected = activeSubTab == 1, onClick = { activeSubTab = 1 }) {
                Text("Payables", Modifier.padding(10.dp), fontSize = 12.sp)
            }
            Tab(selected = activeSubTab == 2, onClick = { activeSubTab = 2 }) {
                Text("Fuel Metrics", Modifier.padding(10.dp), fontSize = 12.sp)
            }
            Tab(selected = activeSubTab == 3, onClick = { activeSubTab = 3 }) {
                Text("Client Invoices", Modifier.padding(10.dp), fontSize = 12.sp)
            }
            Tab(selected = activeSubTab == 4, onClick = { activeSubTab = 4 }) {
                Text("Bank Reconcile", Modifier.padding(10.dp), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                0 -> StaffPayrollSection(viewModel, employees, salaryPayments, baseSymbol)
                1 -> AccountsPayableSection(viewModel, transactions, baseSymbol)
                2 -> FuelLoggerSection(viewModel, vehicles, fuelLogs, baseSymbol)
                3 -> ClientOutgoingInvoicesSection(viewModel, invoices, baseSymbol)
                4 -> BankReconciliationSection(viewModel, transactions, baseSymbol)
            }
        }
    }
}

// ==========================================
// SUB MODULE: STAFF PAYROLL
// ==========================================
@Composable
fun StaffPayrollSection(
    viewModel: ExpenseViewModel,
    employees: List<Employee>,
    payments: List<SalaryPayment>,
    baseSymbol: String
) {
    val context = LocalContext.current
    var showAddEmployee by remember { mutableStateOf(false) }
    var empName by remember { mutableStateOf("") }
    var empRole by remember { mutableStateOf("") }
    var baseSalary by remember { mutableStateOf("") }
    var payFreq by remember { mutableStateOf("MONTHLY") }

    var showPayDialog by remember { mutableStateOf(false) }
    var targetEmpToPay by remember { mutableStateOf<Employee?>(null) }
    var payAmt by remember { mutableStateOf("") }
    var payType by remember { mutableStateOf("REGULAR") } // REGULAR, ADVANCE, BONUS, DEDUCTION
    var payNote by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Payroll / Employee Ledger", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(onClick = { showAddEmployee = true }, shape = RoundedCornerShape(8.dp)) {
                Text("Hire Staff", fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (employees.isEmpty()) {
            Text("No hired staff listed. Register employees to construct payment histories.", fontSize = 12.sp, color = Color.Gray)
        } else {
            employees.forEach { emp ->
                val outstandingAdvances = payments
                    .filter { it.employeeId == emp.id && it.type == "ADVANCE" }
                    .sumOf { it.amount } - payments.filter { it.employeeId == emp.id && it.type == "DEDUCTION" }.sumOf { it.amount }

                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${emp.role} • Salary: $baseSymbol${emp.baseSalary}", fontSize = 11.sp, color = Color.Gray)
                            if (outstandingAdvances > 0) {
                                Text("Outstanding Advance Owed: $baseSymbol$outstandingAdvances", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Button(
                            onClick = {
                                targetEmpToPay = emp
                                payAmt = emp.baseSalary.toString()
                                showPayDialog = true
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Disburse", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Disbursement Logs history", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(8.dp))
        if (payments.isEmpty()) {
            Text("No payments historical logs found.", fontSize = 12.sp, color = Color.Gray)
        } else {
            payments.forEach { pay ->
                val staffName = employees.find { it.id == pay.employeeId }?.name ?: "Unknown"
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Disbursed to: $staffName", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Type: ${pay.type} • Memo: ${pay.note}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Text("-$baseSymbol${pay.amount}", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            }
        }
    }

    if (showAddEmployee) {
        AlertDialog(
            onDismissRequest = { showAddEmployee = false },
            title = { Text("Hire Staff Employee") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = empName, onValueChange = { empName = it }, label = { Text("Full Name") }, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = empRole, onValueChange = { empRole = it }, label = { Text("Job Position Title") }, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = baseSalary, onValueChange = { baseSalary = it }, label = { Text("Contract Base Salary") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("WEEKLY", "MONTHLY").forEach { freq ->
                            FilterChip(selected = payFreq == freq, onClick = { payFreq = freq }, label = { Text(freq) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sal = baseSalary.toDoubleOrNull() ?: 0.0
                        if (empName.isNotEmpty() && sal > 0.0) {
                            viewModel.createEmployee(empName, empRole, sal, payFreq)
                            showAddEmployee = false
                            empName = ""
                            empRole = ""
                            baseSalary = ""
                        }
                    }
                ) { Text("Hire Staff") }
            },
            dismissButton = { TextButton(onClick = { showAddEmployee = false }) { Text("Cancel") } }
        )
    }

    if (showPayDialog && targetEmpToPay != null) {
        val outstandingAdvances = payments
            .filter { it.employeeId == targetEmpToPay!!.id && it.type == "ADVANCE" }
            .sumOf { it.amount } - payments.filter { it.employeeId == targetEmpToPay!!.id && it.type == "DEDUCTION" }.sumOf { it.amount }

        AlertDialog(
            onDismissRequest = { showPayDialog = false },
            title = { Text("Disburse Payroll for " + targetEmpToPay!!.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (outstandingAdvances > 0) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text("Staff owes $baseSymbol$outstandingAdvances in advance floats. Advise Deductions type.", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(value = payAmt, onValueChange = { payAmt = it }, label = { Text("Disbursed Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(10.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        listOf("REGULAR", "ADVANCE", "BONUS", "DEDUCTION").forEach { t ->
                            FilterChip(selected = payType == t, onClick = { payType = t }, label = { Text(t, fontSize = 9.sp) })
                        }
                    }

                    OutlinedTextField(value = payNote, onValueChange = { payNote = it }, label = { Text("Disbursement memo note") }, shape = RoundedCornerShape(10.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payAmt.toDoubleOrNull()
                        if (amt != null && amt > 0.0) {
                            viewModel.makeSalaryPayment(targetEmpToPay!!.id, amt, payType, payNote)
                            showPayDialog = false
                            payAmt = ""
                            payNote = ""
                            Toast.makeText(context, "Payroll disbursed, recorded in expenses!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Confirm payment") }
            },
            dismissButton = { TextButton(onClick = { showPayDialog = false }) { Text("Cancel") } }
        )
    }
}

// ==========================================
// SUB MODULE: ACCOUNTS PAYABLE
// ==========================================
@Composable
fun AccountsPayableSection(viewModel: ExpenseViewModel, transactions: List<Transaction>, baseSymbol: String) {
    val unpaids = transactions.filter { !it.isPaid && it.type == TransactionType.EXPENSE }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Accounts Payable & Vendor Bills Outstanding", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (unpaids.isEmpty()) {
            AppEmptyState(
                title = "No outstanding payables",
                tip = "All vendor purchases and bills are fully cleared.",
                icon = Icons.Default.DoneAll
            )
        } else {
            unpaids.forEach { bill ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(bill.vendorName ?: "General Vendor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Invoice: ${bill.invoiceNumber ?: "N/A"} • Target Date: ${formatDate(bill.date)}", fontSize = 11.sp, color = Color.Gray)
                            if (bill.note.isNotEmpty()) {
                                Text("Memo: " + bill.note, fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("$baseSymbol${bill.amount}", fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    // Mark bill paid by inserting updated item
                                    viewModel.insertTransaction(
                                        accountId = bill.accountId,
                                        amount = bill.amount,
                                        type = bill.type,
                                        categoryId = bill.categoryId,
                                        note = "Cleared Payable bill: " + bill.note,
                                        isPaid = true
                                    )
                                    // Soft delete original unpaid payable leg
                                    viewModel.softDeleteTransaction(bill.id)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(28.dp)
                                    .testTag("submit_button")
                            ) {
                                Text("Clear invoice", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB MODULE: VEHICLE FUEL LOGGER
// ==========================================
@Composable
fun FuelLoggerSection(
    viewModel: ExpenseViewModel,
    vehicles: List<Vehicle>,
    logs: List<FuelLog>,
    baseSymbol: String
) {
    var showCreateVehicle by remember { mutableStateOf(false) }
    var showLogFuel by remember { mutableStateOf(false) }

    var vehName by remember { mutableStateOf("") }
    var fuelType by remember { mutableStateOf("Octane") }
    var registrationPlate by remember { mutableStateOf("") }

    var selectedVehicleForLog by remember { mutableStateOf("") }
    LaunchedEffect(vehicles) {
        if (selectedVehicleForLog.isEmpty() && vehicles.isNotEmpty()) {
            selectedVehicleForLog = vehicles.first().id
        }
    }

    var fuelLiters by remember { mutableStateOf("") }
    var fuelPricePerUnit by remember { mutableStateOf("") }
    var odometerReading by remember { mutableStateOf("") }
    var customMileageFactor by remember { mutableStateOf("") }

    // Interactive Total Cost Calculation
    val quantity = fuelLiters.toDoubleOrNull() ?: 0.0
    val priceUnit = fuelPricePerUnit.toDoubleOrNull() ?: 0.0
    val calculatedTotalCost = quantity * priceUnit

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Fleet & Refuel Analytics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
                Text("Log truck metrics & track fuel economy", fontSize = 11.sp, color = Color(0xFF6B7280))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { showCreateVehicle = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Vehicle", fontSize = 11.sp)
                }
                Button(
                    onClick = { showLogFuel = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B3D)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fuel Up", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (vehicles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFF6B3D).copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No Business Vehicles Tracked", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Create fleet entries to analyze fuel efficiency.", fontSize = 11.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                }
            }
        } else {
            vehicles.forEach { veh ->
                val vehLogs = logs.filter { it.vehicleId == veh.id }.sortedBy { it.date }
                
                // Calculate metrics
                var costPerKm = 0.0
                var calculatedEfficiency = 0.0
                if (vehLogs.size >= 2) {
                    val distanceDiff = vehLogs.last().odometer - vehLogs.first().odometer
                    val totalCost = vehLogs.drop(1).sumOf { it.cost }
                    val totalLiters = vehLogs.drop(1).sumOf { it.liters }
                    if (distanceDiff > 0) {
                        costPerKm = totalCost / distanceDiff
                        if (totalLiters > 0) {
                            calculatedEfficiency = distanceDiff / totalLiters
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF6B3D).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFFFF6B3D), modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(veh.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
                                    Text("Fuel: ${veh.fuelType} • Reg: ${veh.registration ?: "N/A"}", fontSize = 11.sp, color = Color(0xFF6B7280))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Odometer Reading", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text("${vehLogs.lastOrNull()?.odometer ?: 0.0} km", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Fill-Up Incidents", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text("${vehLogs.size} records", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Avg Efficiency", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text(if (calculatedEfficiency > 0.0) String.format("%.2f km/L", calculatedEfficiency) else "N/A", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF6B3D))
                            }
                        }

                        if (costPerKm > 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Computed running cost rate:", fontSize = 11.sp, color = Color(0xFF6B7280))
                                Text(
                                    text = "$baseSymbol${String.format("%.2f", costPerKm)} / km",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        // Detailed Refuel Receipts Timeline
                        if (vehLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Recent Refuel History", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF111827))
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            vehLogs.reversed().take(3).forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${log.liters} L refilled @ $baseSymbol${log.pricePerUnit}/L", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                                        Text("Odometer: ${log.odometer} km • Efficiency: ${log.mileage} km/L", fontSize = 9.sp, color = Color(0xFF9CA3AF))
                                    }
                                    Text(
                                        text = "$baseSymbol${log.cost.toInt()}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF111827)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateVehicle) {
        AlertDialog(
            onDismissRequest = { showCreateVehicle = false },
            title = { Text("Add Fleet Vehicle", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = vehName,
                        onValueChange = { vehName = it },
                        label = { Text("Make/Model name") },
                        placeholder = { Text("e.g. Scania Heavy Truck") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fuelType,
                        onValueChange = { fuelType = it },
                        label = { Text("Fuel Grade/Type") },
                        placeholder = { Text("e.g. Diesel, Octane 98") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = registrationPlate,
                        onValueChange = { registrationPlate = it },
                        label = { Text("Registration Identifier Plate") },
                        placeholder = { Text("e.g. TX-492-99") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (vehName.isNotEmpty()) {
                            viewModel.createVehicle(vehName, fuelType, registrationPlate.ifEmpty { null })
                            showCreateVehicle = false
                            vehName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B3D))
                ) { Text("Save transport") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateVehicle = false }) { Text("Cancel", color = Color(0xFF9CA3AF)) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showLogFuel) {
        AlertDialog(
            onDismissRequest = { showLogFuel = false },
            title = { Text("Log refuel receipts", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Select transport vehicle:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF374151))
                    Card(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Row(modifier = Modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            vehicles.forEach { v ->
                                FilterChip(
                                    selected = selectedVehicleForLog == v.id,
                                    onClick = { selectedVehicleForLog = v.id },
                                    label = { Text(v.name, fontSize = 9.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = fuelLiters,
                        onValueChange = { fuelLiters = it },
                        label = { Text("Volume (Liters / Gallons)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fuelPricePerUnit,
                        onValueChange = { fuelPricePerUnit = it },
                        label = { Text("Price per unit ($baseSymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = odometerReading,
                        onValueChange = { odometerReading = it },
                        label = { Text("Odometer Reading (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customMileageFactor,
                        onValueChange = { customMileageFactor = it },
                        label = { Text("Calculated Trip Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        placeholder = { Text("Optional distance since last fill") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Dynamically Calculated Total Cost Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFF6B3D).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("Automatic Total Budget Deduction Cost:", fontSize = 10.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$baseSymbol${String.format("%.2f", calculatedTotalCost)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFFFF6B3D)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lit = fuelLiters.toDoubleOrNull()
                        val unitPrice = fuelPricePerUnit.toDoubleOrNull() ?: 0.0
                        val odo = odometerReading.toDoubleOrNull()
                        val tripDist = customMileageFactor.toDoubleOrNull() ?: 0.0
                        
                        // Calculate mileage efficiency: distance / liters
                        val computedMileageVal = if (lit != null && lit > 0.0 && tripDist > 0.0) {
                            tripDist / lit
                        } else {
                            0.0
                        }

                        if (lit != null && calculatedTotalCost > 0.0 && odo != null && selectedVehicleForLog.isNotEmpty()) {
                            viewModel.logFuel(
                                vehicleId = selectedVehicleForLog,
                                liters = lit,
                                cost = calculatedTotalCost,
                                odometer = odo,
                                fuelType = "Petrol",
                                pricePerUnit = unitPrice,
                                mileage = computedMileageVal
                            )
                            showLogFuel = false
                            fuelLiters = ""
                            fuelPricePerUnit = ""
                            odometerReading = ""
                            customMileageFactor = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B3D))
                ) { Text("Register gas receipts") }
            },
            dismissButton = {
                TextButton(onClick = { showLogFuel = false }) { Text("Cancel", color = Color(0xFF9CA3AF)) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ==========================================
// SUB MODULE: CLIENT INVOICING (ACCOUNTS RECEIVABLES - AR)
// ==========================================
@Composable
fun ClientOutgoingInvoicesSection(
    viewModel: ExpenseViewModel,
    invoices: List<Invoice>,
    baseSymbol: String
) {
    val context = LocalContext.current
    var showCreateInvoice by remember { mutableStateOf(false) }
    var clientName by remember { mutableStateOf("") }
    var clientContact by remember { mutableStateOf("") }
    var taxRate by remember { mutableStateOf("15") }

    // Draft line item states
    var draftLines = remember { mutableStateListOf<InvoiceItemDraft>() }
    var tempDesc by remember { mutableStateOf("") }
    var tempQty by remember { mutableStateOf("") }
    var tempPrice by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Outgoing Client Bill Invoices (AR)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Button(onClick = { showCreateInvoice = true }, shape = RoundedCornerShape(8.dp)) {
                Text("Generate Client Bill", fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (invoices.isEmpty()) {
            AppEmptyState(
                title = "No receivables listed",
                tip = "Track pending customer invoice items. Settle payments with automatic income additions.",
                icon = Icons.Default.FileCopy
            )
        } else {
            invoices.forEach { inv ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(inv.invoiceNumber, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Box(modifier = Modifier.background(Color(if (inv.status == "PAID") 0xFF4CAF50 else 0xFFFFB300).copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text(inv.status, color = Color(if (inv.status == "PAID") 0xFF4CAF50 else 0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Deliver to: " + inv.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Contact: ${inv.clientContact} • Tax rate: ${inv.taxRate}%", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            if (inv.status != "PAID") {
                                Button(
                                    onClick = {
                                        viewModel.markInvoiceAsPaid(inv.id)
                                        Toast.makeText(context, "Invoice settled, Income auto logged!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    modifier = Modifier.height(28.dp)
                                        .testTag("submit_button")
                                ) {
                                    Text("Mark Paid Receives", fontSize = 10.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            // Share Mock Generated Invoice PDF
                            IconButton(onClick = { shareMockInvoicePdf(context, inv) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateInvoice) {
        AlertDialog(
            onDismissRequest = { showCreateInvoice = false },
            title = { Text("Generate Invoice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Business Client Name") }, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = clientContact, onValueChange = { clientContact = it }, label = { Text("Address / Tel Contact") }, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = taxRate, onValueChange = { taxRate = it }, label = { Text("Government VAT Tax %") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(10.dp))

                    Text("Invoice Line Items additions (" + draftLines.size + " added)", fontWeight = FontWeight.Bold)

                    // Draft addition
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = tempDesc, onValueChange = { tempDesc = it }, label = { Text("Line Item Description") }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth())
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedTextField(value = tempQty, onValueChange = { tempQty = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f))
                                OutlinedTextField(value = tempPrice, onValueChange = { tempPrice = it }, label = { Text("Unit Price") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f))
                            }
                            Button(
                                onClick = {
                                    val q = tempQty.toDoubleOrNull() ?: 1.0
                                    val p = tempPrice.toDoubleOrNull() ?: 0.0
                                    if (tempDesc.isNotEmpty() && p > 0.0) {
                                        draftLines.add(InvoiceItemDraft(tempDesc, q, p))
                                        tempDesc = ""
                                        tempQty = ""
                                        tempPrice = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add Line record")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tRate = taxRate.toDoubleOrNull() ?: 0.0
                        if (clientName.isNotEmpty() && draftLines.isNotEmpty()) {
                            viewModel.createOutgoingInvoice(clientName, clientContact, draftLines.toList(), tRate, System.currentTimeMillis())
                            showCreateInvoice = false
                            clientName = ""
                            clientContact = ""
                            draftLines.clear()
                        }
                    }
                ) { Text("Confirm Invoice bill") }
            },
            dismissButton = { TextButton(onClick = { showCreateInvoice = false }) { Text("Cancel") } }
        )
    }
}

// In-Memory Print Mock Invoice file share
private fun shareMockInvoicePdf(context: Context, invoice: Invoice) {
    try {
        val file = File(context.cacheDir, "inv_${invoice.invoiceNumber}.txt")
        val stream = FileOutputStream(file)
        val template = """
            =================================================
            Invoice Number: ${invoice.invoiceNumber}
            Client Name: ${invoice.clientName}
            Contact: ${invoice.clientContact}
            Date: ${formatDate(invoice.date)}
            =================================================
            Tax rate applied: ${invoice.taxRate}%
            Status: ${invoice.status}
            =================================================
            Thank you for your offline transaction backing!
        """.trimIndent()
        stream.write(template.toByteArray())
        stream.close()

        val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Client Invoice text file"))
    } catch (e: Exception) {
        Toast.makeText(context, "Sharing invoice crashed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ==========================================
// SUB MODULE: BANK STATEMENT RECONCILIATION
// ==========================================
@Composable
fun BankReconciliationSection(
    viewModel: ExpenseViewModel,
    transactions: List<Transaction>,
    baseSymbol: String
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Bank Statement Reconciliation checklist", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Compare physical bank statements against matching logged offline entries. reconcile checkbox ensures account consistency.", fontSize = 11.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Text("No transactions logged to reconcile yet.", fontSize = 12.sp, color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(transactions) { txn ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(txn.note.ifEmpty { "General Transaction" }, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Amount: $baseSymbol${txn.amount} • Date: ${formatDate(txn.date)}", fontSize = 11.sp, color = Color.Gray)
                                if (txn.reconciled) {
                                    Text("Reconciled checkmark settled in books!", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = txn.reconciled, onCheckedChange = { viewModel.toggleReconcileStatus(txn.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
