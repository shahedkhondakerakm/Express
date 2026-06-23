package com.example.ui.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.util.translated
import com.example.ui.util.LocalAppLanguage
import java.util.*

// ==========================================
// ACCOUNTS MODULE SCREEN
// ==========================================
@Composable
fun AccountsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val activeSpace by viewModel.activeSpace.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"

    var showCreateDialog by remember { mutableStateOf(false) }
    var accountName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.CASH) }
    var initialBalance by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Asset Wallets / Accounts", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (accounts.isEmpty()) {
            AppEmptyState(
                title = "No linked accounts",
                tip = "Accounts represent Cash wallets, Bank savings accounts, Credit cards, or digital E-Wallets. Tap 'Add' to begin.",
                icon = Icons.Default.AccountBalance
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(accounts) { acc ->
                    val balance = viewModel.calculateAccountBalance(acc)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(acc.type) {
                                            AccountType.CASH -> Icons.Default.Money
                                            AccountType.BANK -> Icons.Default.AccountBalance
                                            AccountType.CREDIT_CARD -> Icons.Default.Payment
                                            AccountType.E_WALLET -> Icons.Default.Smartphone
                                            else -> Icons.Default.CardMembership
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(acc.type.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("$baseSymbol${String.format("%,.2f", balance)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                IconButton(
                                    onClick = {
                                        viewModel.archiveAccount(acc.id)
                                        Toast.makeText(context, "Account archived", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Archive, contentDescription = "Archive", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Open Account") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Label Name") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Wallet Account Type:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AccountType.values().take(4).forEach { type ->
                            FilterChip(
                                selected = accountType == type,
                                onClick = { accountType = type },
                                label = { Text(type.name, fontSize = 9.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Starting / Opening Float Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedBal = initialBalance.toDoubleOrNull() ?: 0.0
                        if (accountName.isNotEmpty()) {
                            viewModel.createAccount(accountName, accountType, activeSpace?.currency ?: "USD", parsedBal)
                            showCreateDialog = false
                            accountName = ""
                            initialBalance = ""
                        }
                    }
                ) {
                    Text("Save Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// CATEGORIES MODULE SCREEN
// ==========================================
@Composable
fun CategoriesScreen(viewModel: ExpenseViewModel) {
    val categories by viewModel.categories.collectAsState()
    var showAddCategory by remember { mutableStateOf(false) }
    var isIncomeType by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var chosenIcon by remember { mutableStateOf(IconLoader.availableIcons.first()) }
    var chosenColorHex by remember { mutableStateOf(0xFF00B276.toInt()) }

    val presetColors = listOf(
        0xFFE57373.toInt(), 0xFFF06292.toInt(), 0xFFBA68C8.toInt(), 0xFF9575CD.toInt(),
        0xFF64B5F6.toInt(), 0xFF4FC3F7.toInt(), 0xFF4DD0E1.toInt(), 0xFF4DB6AC.toInt(),
        0xFF81C784.toInt(), 0xFFAED581.toInt(), 0xFFFFD54F.toInt(), 0xFFFFB74D.toInt()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Label Categories", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Button(
                onClick = { showAddCategory = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Type list
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isIncomeType,
                onClick = { isIncomeType = false },
                label = { Text("Expense Categories", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isIncomeType,
                onClick = { isIncomeType = true },
                label = { Text("Income Categories", fontSize = 12.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val displayedCategories = categories.filter { it.isIncome == isIncomeType }
        if (displayedCategories.isEmpty()) {
            AppEmptyState(
                title = "No categories yet",
                tip = "Categories help segment transactions in visual metrics graphs. Tap 'Add' to create.",
                icon = Icons.Default.Category
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayedCategories) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(cat.color).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconLoader.getIcon(cat.icon),
                                        contentDescription = null,
                                        tint = Color(cat.color)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            IconButton(onClick = { viewModel.archiveCategory(cat.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Archive", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("Create Category Tag") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Tag Name") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pick Category Color:", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetColors.take(6).forEach { colorHex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorHex))
                                    .border(
                                        width = if (chosenColorHex == colorHex) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                                    .clickable { chosenColorHex = colorHex }
                            )
                        }
                    }

                    Text("Pick Category Symbol Icon:", fontWeight = FontWeight.SemiBold)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp)
                        ) {
                            IconLoader.availableIcons.chunked(4).forEach { rowIcons ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowIcons.forEach { ic ->
                                        val isSelected = chosenIcon == ic
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                                .clickable { chosenIcon = ic }
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconLoader.getIcon(ic),
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotEmpty()) {
                            viewModel.createCategory(newCategoryName, chosenIcon, chosenColorHex, isIncomeType)
                            showAddCategory = false
                            newCategoryName = ""
                        }
                    }
                ) {
                    Text("Add tag")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// TRANSACTIONS MODULE SCREEN (SEARCH/FILTER)
// ==========================================
@Composable
fun TransactionsScreen(viewModel: ExpenseViewModel) {
    val search by viewModel.searchText.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val activeSpace by viewModel.activeSpace.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Active, 1 = Soft Deleted Trash

    // Calendar Widget Logic
    val calendar = Calendar.getInstance()
    val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    var selectedDay by remember { mutableStateOf(currentDayOfMonth) }

    // Calculate income & expenses for selected Day/Month
    val selectedDayTxns = transactions.filter {
        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
        cal.get(Calendar.DAY_OF_MONTH) == selectedDay
    }
    
    val dayIncome = selectedDayTxns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val dayExpense = selectedDayTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TRANSACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Expenses & Calendars",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            }
            
            if (selectedTabIndex == 1 && recentlyDeleted.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearTrash() }) {
                    Text("Empty Trash", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // CALENDAR WIDGET (Modern Monthly Calendar Day Row Selector)
        Column {
            Text(
                text = "Calendar Day Vector",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(maxDays) { index ->
                    val dayNum = index + 1
                    val isSelected = selectedDay == dayNum
                    
                    // day of week name representation
                    val dayCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, dayNum) }
                    val dayName = when (dayCal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "Su"
                        Calendar.MONDAY -> "Mo"
                        Calendar.TUESDAY -> "Tu"
                        Calendar.WEDNESDAY -> "We"
                        Calendar.THURSDAY -> "Th"
                        Calendar.FRIDAY -> "Fr"
                        else -> "Sa"
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 74.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) Color(0xFFFF6B3D) else Color.White
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFFFF6B3D) else Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedDay = dayNum }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = dayNum.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF111827)
                            )
                        }
                    }
                }
            }
        }

        // SUMMARY CARDS (Side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Total Income (Purple Gradient #7C3AED -> #9333EA)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DAY INCOME",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "$baseSymbol${String.format("%,.1f", dayIncome)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Deposit ledger",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Card 2: Total Expenses (Orange Gradient #FF6B3D -> #FF7A45)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(115.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFFFF6B3D), Color(0xFFFF7A45))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DAY EXPENSE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "$baseSymbol${String.format("%,.1f", dayExpense)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Output total",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // EXPENSE CATEGORIES TARGET BUDGETS & PROGRESS BAR
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Category Budget Burn",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                modifier = Modifier.padding(bottom = 2.dp)
            )

            // Let's list general default categories or custom categories
            val expensesOnly = transactions.filter { it.type == TransactionType.EXPENSE }
            val spentMap = expensesOnly.groupBy { it.categoryId }.mapValues { it.value.sumOf { tx -> tx.amount } }

            categories.filter { !it.isIncome }.take(6).forEach { cat ->
                val spent = spentMap[cat.id] ?: 0.0
                val budgetLimit = 500.0 // Default or standard fallback limit
                val pct = if (budgetLimit > 0) (spent / budgetLimit).coerceIn(0.0, 1.0) else 0.0
                val pctInt = (pct * 100).toInt()
                val colHex = cat.color.toLong()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(colHex).copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconLoader.getIcon(cat.icon),
                                        contentDescription = null,
                                        tint = Color(colHex),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = cat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        text = "Spent $baseSymbol${spent.toInt()} of $baseSymbol${budgetLimit.toInt()}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                            Text(
                                text = "$pctInt%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF111827)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Progress Bar: Height 12px, Radius 20px, light gray background, purple gradient progress
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFE5E7EB))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(pct.toFloat())
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // TABS TOGGLE FOR THE ACTIVE TRANSACTION LEDGERS OR TRASH BIN
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = Color(0xFF7C3AED),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF7C3AED)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 }
                    ) {
                        Text("Active Records", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 }
                    ) {
                        Text("Trash Bin (${recentlyDeleted.size})", modifier = Modifier.padding(12.dp), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SEARCH BAR
                OutlinedTextField(
                    value = search,
                    onValueChange = { viewModel.searchText.value = it },
                    label = { Text("Search logs", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    val filteredTxns = transactions.filter {
                        it.note.contains(search, ignoreCase = true) ||
                        (categories.find { c -> c.id == it.categoryId }?.name?.contains(search, ignoreCase = true) ?: false) ||
                        it.amount.toString().contains(search)
                    }

                    if (filteredTxns.isEmpty()) {
                        AppEmptyState(
                            title = "No transactions matches",
                            tip = "There are no transactions logged for this selected scope.",
                            icon = Icons.Default.ReceiptLong
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredTxns.forEach { txn ->
                                val cat = categories.find { it.id == txn.categoryId }
                                val acc = accounts.find { it.id == txn.accountId }
                                val colHex = cat?.color?.toLong() ?: 0xFF9CA3AF

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(14.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(colHex).copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconLoader.getIcon(cat?.icon ?: "Receipt"),
                                                contentDescription = null,
                                                tint = Color(colHex),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = txn.note.ifEmpty { cat?.name ?: "Transfer" },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                text = "${acc?.name ?: "Cash"} • ${formatDate(txn.date)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (txn.type == TransactionType.INCOME) "+$baseSymbol${String.format("%,.1f", txn.amount)}" else "-$baseSymbol${String.format("%,.1f", txn.amount)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (txn.type == TransactionType.INCOME) Color(0xFF10B981) else Color(0xFFFF6B3D)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { viewModel.softDeleteTransaction(txn.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Trash", tint = Color(0xFFFF6B3D).copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (recentlyDeleted.isEmpty()) {
                        AppEmptyState(
                            title = "Trash of transactions is empty",
                            tip = "No soft deleted items present.",
                            icon = Icons.Default.DeleteOutline
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentlyDeleted.forEach { txn ->
                                val cat = categories.find { it.id == txn.categoryId }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(14.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(txn.note.ifEmpty { cat?.name ?: "Transfer Record" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF111827))
                                        Text("Deleted: ${formatDate(txn.deletedAt ?: System.currentTimeMillis())}", fontSize = 10.sp, color = Color(0xFF6B7280))
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        TextButton(onClick = { viewModel.restoreTransaction(txn.id) }) {
                                            Text("Restore", fontSize = 11.sp, color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(onClick = { viewModel.permanentlyDeleteTransaction(txn.id) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Purge", tint = Color(0xFFFF6B3D), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// BUDGETS ALERTS MODULE
// ==========================================
@Composable
fun BudgetsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val budgets by viewModel.budgets.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val activeSpace by viewModel.activeSpace.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"

    // Global aggregations for the Centralized Dashboards
    val allSpaces by viewModel.allSpaces.collectAsState()
    val allBudgets by viewModel.allBudgets.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0 = Active Workspace, 1 = Centralized Dashboard

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull { !it.isIncome }?.id) }
    var budgetLimit by remember { mutableStateOf("") }
    var period by remember { mutableStateOf("MONTHLY") }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null || categories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = categories.firstOrNull { !it.isIncome }?.id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Style Screen Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Resource Allocator".translated(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111827),
                    fontSize = 22.sp
                )
                Text(
                    text = if (activeTab == 0) "Set threshold limits for ${activeSpace?.name ?: "active profile"}" else "Consolidated dashboard across all active workspaces",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280)
                )
            }
            if (activeTab == 0) {
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Limit".translated(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Premium Cohesive Pill-selector TabRow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF3F4F6))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activeTab == 0) Color.White else Color.Transparent)
                    .clickable { activeTab = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (activeTab == 0) Color(0xFF7C3AED) else Color(0xFF6B7280),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Profiles".translated(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == 0) Color(0xFF111827) else Color(0xFF6B7280)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activeTab == 1) Color.White else Color.Transparent)
                    .clickable { activeTab = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dashboard,
                        contentDescription = null,
                        tint = if (activeTab == 1) Color(0xFF7C3AED) else Color(0xFF6B7280),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Global Dashboard".translated(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == 1) Color(0xFF111827) else Color(0xFF6B7280)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (activeTab == 0) {
            // TAB 1: WORKSPACE LOCAL LIMITS
            if (budgets.isEmpty()) {
                AppEmptyState(
                    title = "Set Category Limits",
                    tip = "Keep expenditure in control! Allocate a total recurring maximum limit for categories here to secure warnings at 80% and over-draft flags at 100%.",
                    icon = Icons.Default.AccountBalanceWallet
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(budgets) { budget ->
                        val cat = categories.find { it.id == budget.categoryId }
                        val currentSpending = transactions
                            .filter { it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                        val progress = if (budget.limitAmount > 0) (currentSpending / budget.limitAmount).toFloat() else 0f

                        val isOverBudget = progress >= 1.0f
                        val isWarnBudget = progress >= 0.8f && progress < 1.0f

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    color = when {
                                                        isOverBudget -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                                        isWarnBudget -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                                        else -> Color(0xFF10B981).copy(alpha = 0.1f)
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = IconLoader.getIcon(cat?.icon ?: "Receipt"),
                                                contentDescription = null,
                                                tint = when {
                                                    isOverBudget -> Color(0xFFEF4444)
                                                    isWarnBudget -> Color(0xFFF59E0B)
                                                    else -> Color(0xFF10B981)
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = cat?.name ?: "General Space Budget",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF111827)
                                            )
                                            Text(
                                                text = "Recurring: ${budget.period}",
                                                fontSize = 9.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteBudget(budget) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = when {
                                        isOverBudget -> Color(0xFFEF4444)
                                        isWarnBudget -> Color(0xFFF59E0B)
                                        else -> Color(0xFF10B981)
                                    },
                                    trackColor = Color(0xFFF3F4F6)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Used: ".translated(),
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                        Text(
                                            text = "$baseSymbol${currentSpending.toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOverBudget) Color(0xFFEF4444) else Color(0xFF111827)
                                        )
                                    }
                                    Text(
                                        text = "Limit: ".translated() + "$baseSymbol${budget.limitAmount.toInt()}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF374151),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: CENTRALIZED GLOBAL BUDGETING DASHBOARD (Aggregates expenses from all spaces)
            val aggregatedLimit = allBudgets.sumOf { it.limitAmount }
            val aggregatedSpent = allBudgets.sumOf { budget ->
                allTransactions
                    .filter { it.spaceId == budget.spaceId && it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
            }
            val aggregatedRemaining = (aggregatedLimit - aggregatedSpent).coerceAtLeast(0.0)
            val globalProgress = if (aggregatedLimit > 0) (aggregatedSpent / aggregatedLimit).toFloat() else 0f

            val overbudgetsCount = allBudgets.count { budget ->
                val spent = allTransactions
                    .filter { it.spaceId == budget.spaceId && it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                spent >= budget.limitAmount
            }

            val warningBudgetsCount = allBudgets.count { budget ->
                val spent = allTransactions
                    .filter { it.spaceId == budget.spaceId && it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                spent >= (budget.limitAmount * 0.8) && spent < budget.limitAmount
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // 1. Consolidated High-fidelity Graphic Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)) // Rich Dark Theme Core Card
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (overbudgetsCount > 0) Color(0xFFEF4444) else Color(
                                                    0xFF10B981
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CONSOLIDATED LIMIT ENGINE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9CA3AF),
                                        letterSpacing = 1.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF374151), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${allBudgets.size} Space Limits",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Remaining Global Budget",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "$baseSymbol${String.format("%,.0f", aggregatedRemaining)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom Radial/Track progress bar for entire App limits
                            LinearProgressIndicator(
                                progress = { globalProgress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape),
                                color = when {
                                    globalProgress >= 1.0f -> Color(0xFFEF4444)
                                    globalProgress >= 0.8f -> Color(0xFFFFB300)
                                    else -> Color(0xFF10B981)
                                },
                                trackColor = Color(0xFF1F2937)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Spent Globally", fontSize = 9.sp, color = Color(0xFF9CA3AF))
                                    Text("$baseSymbol${aggregatedSpent.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Approved Cap limit", fontSize = 9.sp, color = Color(0xFF9CA3AF))
                                    Text("$baseSymbol${aggregatedLimit.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 2. Alert Quick Flags (Over-utilization warnings)
                if (overbudgetsCount > 0 || warningBudgetsCount > 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (overbudgetsCount > 0) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("$overbudgetsCount limits", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF991B1B))
                                            Text("Overlimit", fontSize = 9.sp, color = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }

                            if (warningBudgetsCount > 0) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("$warningBudgetsCount warning", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                                            Text("Above 80%", fontSize = 9.sp, color = Color(0xFFF59E0B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Profiles breakdown listing
                item {
                    Text(
                        text = "Workspace Profile Breakdowns",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                items(allSpaces) { space ->
                    val spaceBudgets = allBudgets.filter { it.spaceId == space.id }
                    val spaceLimit = spaceBudgets.sumOf { it.limitAmount }
                    val spaceSpent = spaceBudgets.sumOf { b ->
                        allTransactions
                            .filter { it.spaceId == space.id && it.categoryId == b.categoryId && it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                    }
                    val spaceRemaining = (spaceLimit - spaceSpent).coerceAtLeast(0.0)
                    val spaceProgress = if (spaceLimit > 0) (spaceSpent / spaceLimit).toFloat() else 0f

                    val spaceSymbolLocal = space.currency.ifEmpty { "USD" }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Section Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7C3AED).copy(alpha = 0.08f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (space.type) {
                                                SpaceType.PERSONAL -> Icons.Default.Person
                                                SpaceType.FAMILY -> Icons.Default.Group
                                                SpaceType.BUSINESS -> Icons.Default.Business
                                            },
                                            contentDescription = null,
                                            tint = Color(0xFF7C3AED),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = space.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF111827)
                                        )
                                        Text(
                                            text = "${space.type.name} Profile",
                                            fontSize = 9.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }

                                // Quick switch contextual launcher
                                OutlinedButton(
                                    onClick = {
                                        viewModel.switchActiveSpace(space.id)
                                        activeTab = 0
                                        Toast.makeText(context, "Switched view to ${space.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Switch Context", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (spaceBudgets.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "No limits set. Use 'Switch Context' to plan budgets.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            } else {
                                // Linear progress for this Profile
                                LinearProgressIndicator(
                                    progress = { spaceProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = when {
                                        spaceProgress >= 1.0f -> Color(0xFFEF4444)
                                        spaceProgress >= 0.8f -> Color(0xFFFFB300)
                                        else -> Color(0xFF10B981)
                                    },
                                    trackColor = Color(0xFFF3F4F6)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Remaining: $spaceSymbolLocal ${spaceRemaining.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF374151)
                                    )
                                    Text(
                                        text = "Allocated: $spaceSymbolLocal ${spaceLimit.toInt()}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Expanded items breakdown list inside the space card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        spaceBudgets.forEach { b ->
                                            val cat = allCategories.find { it.id == b.categoryId }
                                            val spent = allTransactions
                                                .filter { it.spaceId == space.id && it.categoryId == b.categoryId && it.type == TransactionType.EXPENSE }
                                                .sumOf { it.amount }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = IconLoader.getIcon(cat?.icon ?: "Receipt"),
                                                        contentDescription = null,
                                                        tint = Color(0xFF4B5563),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = cat?.name ?: "Category Limit",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF374151)
                                                    )
                                                }

                                                Text(
                                                    text = "$spaceSymbolLocal${spent.toInt()} / $spaceSymbolLocal${b.limitAmount.toInt()}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (spent >= b.limitAmount) Color(0xFFEF4444) else Color(0xFF111827)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Set Expense Budget", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Select Category:")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.filter { !it.isIncome }.forEach { cat ->
                                val isSelected = cat.id == selectedCategoryId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = budgetLimit,
                        onValueChange = { budgetLimit = it },
                        label = { Text("Budget Limit Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("WEEKLY", "MONTHLY", "YEARLY").forEach { p ->
                            FilterChip(
                                selected = period == p,
                                onClick = { period = p },
                                label = { Text(p, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitVal = budgetLimit.toDoubleOrNull() ?: 0.0
                        if (limitVal > 0 && selectedCategoryId != null) {
                            viewModel.createBudget(selectedCategoryId, limitVal, period, listOf(0.8, 1.0))
                            showCreateDialog = false
                            budgetLimit = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Set Limit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel", color = Color(0xFF9CA3AF)) }
            }
        )
    }
}

// ==========================================
// SAVINGS GOALS MODULE
// ==========================================
@Composable
fun SavingsGoalsScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val goals by viewModel.savingsGoals.collectAsState()
    val activeSpace by viewModel.activeSpace.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"

    var showCreateDialog by remember { mutableStateOf(false) }
    var goalName by remember { mutableStateOf("") }
    var targetAmt by remember { mutableStateOf("") }
    
    // Quick contribute state
    var showContributeDialog by remember { mutableStateOf(false) }
    var targetGoalForContribute by remember { mutableStateOf<SavingsGoal?>(null) }
    var contributionAmt by remember { mutableStateOf("") }

    // Celebratory Signal listener
    LaunchedEffect(key1 = true) {
        viewModel.goalCelebratedSignal.collect { goalName ->
            Toast.makeText(context, "🎉 Celebration! You have achieved your savings goal: $goalName!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Savings Goals Track", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Button(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (goals.isEmpty()) {
            AppEmptyState(
                title = "No goals added",
                tip = "Track your major financial milestones (like 'Vacation Fund' or 'House deposit'). Add contributions over time.",
                icon = Icons.Default.Savings
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(goals) { goal ->
                    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(goal.color).copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconLoader.getIcon(goal.icon),
                                            contentDescription = null,
                                            tint = Color(goal.color)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            if (goal.isCompleted) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(modifier = Modifier.background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                    Text("Won 🎉", fontSize = 9.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text("Progress: ${(progress * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            targetGoalForContribute = goal
                                            showContributeDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.VolunteerActivism, contentDescription = "Contribute", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteGoal(goal) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = if (goal.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$baseSymbol${goal.currentAmount.toInt()} contributed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Target: $baseSymbol${goal.targetAmount.toInt()}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Open Savings Goal") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Goal Title Target") },
                        placeholder = { Text("e.g. New Car Fund") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetAmt,
                        onValueChange = { targetAmt = it },
                        label = { Text("Target Goal Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val tarVal = targetAmt.toDoubleOrNull() ?: 0.0
                        if (goalName.isNotEmpty() && tarVal > 0) {
                            viewModel.createSavingsGoal(goalName, tarVal, null, "Savings", 0xFF00B276.toInt())
                            showCreateDialog = false
                            goalName = ""
                            targetAmt = ""
                        }
                    }
                ) {
                    Text("Create Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showContributeDialog && targetGoalForContribute != null) {
        AlertDialog(
            onDismissRequest = { showContributeDialog = false },
            title = { Text("Contribute to Goal") },
            text = {
                OutlinedTextField(
                    value = contributionAmt,
                    onValueChange = { contributionAmt = it },
                    label = { Text("Contribution Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = contributionAmt.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.contributeToGoal(targetGoalForContribute!!, amt)
                            showContributeDialog = false
                            contributionAmt = ""
                        }
                    }
                ) {
                    Text("Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContributeDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
