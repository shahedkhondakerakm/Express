package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.model.TransactionType
import com.example.data.model.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screen.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.translated

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppEntry()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppEntry() {
    val context = LocalContext.current
    val viewModel: ExpenseViewModel = viewModel {
        ExpenseViewModel(context.applicationContext as android.app.Application)
    }

    val isOnboardingComplete by viewModel.isOnboardingComplete.collectAsState()
    val pinLockCode by viewModel.pinLockCode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    CompositionLocalProvider(LocalAppLanguage provides currentLang) {
        var isAppLocked by remember { mutableStateOf(false) }
        var pinValue by remember { mutableStateOf("") }

        LaunchedEffect(pinLockCode, isOnboardingComplete) {
            if (pinLockCode != null && isOnboardingComplete) {
                isAppLocked = true
            }
        }

        if (!isOnboardingComplete) {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    // Done
                }
            )
        } else if (isAppLocked) {
        // Absolute full-screen security barrier
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Database Secured", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter your 4-digit security PIN to access your personal workspace metrics.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = pinValue,
                    onValueChange = {
                        if (it.length <= 4) {
                            pinValue = it
                            if (it.length == 4) {
                                if (pinLockCode == it) {
                                    isAppLocked = false
                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Invalid Security PIN!", Toast.LENGTH_SHORT).show()
                                    pinValue = ""
                                }
                            }
                        }
                    },
                    label = { Text("4-Digit PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.width(180.dp).testTag("username_input")
                )
            }
        }
    } else {
        // Core application screens with bottom navigation
        val navController = rememberNavController()
        var currentRoute by remember { mutableStateOf("dashboard") }
        var showGlobalAddDialog by remember { mutableStateOf(false) }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route ?: "dashboard"
        }

        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home tab
                        val isHome = currentRoute == "dashboard"
                        IconButton(
                            onClick = {
                                navController.navigate("dashboard") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isHome) Icons.Default.Home else Icons.Default.Home, // Outlined style where possible
                                    contentDescription = "Home",
                                    tint = if (isHome) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Home".translated(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHome) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                            }
                        }

                        // Expenses tab
                        val isExpenses = currentRoute == "transactions"
                        IconButton(
                            onClick = {
                                navController.navigate("transactions") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Expenses",
                                    tint = if (isExpenses) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Expenses".translated(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpenses) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                            }
                        }

                        // Elevated center FAB placeholder space
                        Spacer(modifier = Modifier.weight(1f))

                        // Analytics tab
                        val isAnalytics = currentRoute == "reports"
                        IconButton(
                            onClick = {
                                navController.navigate("reports") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Analytics",
                                    tint = if (isAnalytics) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Analytics".translated(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnalytics) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                            }
                        }

                        // Profile tab
                        val isProfile = currentRoute == "settings"
                        IconButton(
                            onClick = {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = if (isProfile) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Profile".translated(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProfile) Color(0xFFFF6B3D) else Color(0xFFD1D5DB)
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showGlobalAddDialog = true },
                    modifier = Modifier
                        .size(64.dp)
                        .offset(y = 44.dp)
                        .testTag("global_add_fab"),
                    shape = CircleShape,
                    containerColor = Color(0xFFFF6B3D),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 15.dp,
                        hoveredElevation = 12.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // RENDER GLOBAL DIALOGUE
                val accounts by viewModel.accounts.collectAsState()
                val categories by viewModel.categories.collectAsState()
                val familyMembers by viewModel.familyMembers.collectAsState()
                val activeSpace by viewModel.activeSpace.collectAsState()
                val baseSymbol = activeSpace?.currency ?: "$"

                if (showGlobalAddDialog) {
                    var amountStr by remember { mutableStateOf("") }
                    var tempTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
                    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
                    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
                    var noteStr by remember { mutableStateOf("") }
                    
                    var destinationAccountId by remember { mutableStateOf(accounts.firstOrNull { it.id != selectedAccountId }?.id ?: "") }
                    var transferFeeStr by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { showGlobalAddDialog = false },
                        title = { Text("Log Transaction Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF111827)) },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Type selection
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (t in TransactionType.values()) {
                                        val isSelected = tempTransactionType == t
                                        val color = when (t) {
                                            TransactionType.INCOME -> Color(0xFF10B981)
                                            TransactionType.EXPENSE -> Color(0xFFFF6B3D)
                                            TransactionType.TRANSFER -> Color(0xFF7C3AED)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) color else Color(0xFFF3F4F6))
                                                .clickable { tempTransactionType = t }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = t.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isSelected) Color.White else Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }

                                // Amount Input
                                OutlinedTextField(
                                    value = amountStr,
                                    onValueChange = { amountStr = it },
                                    label = { Text("Amount ($baseSymbol)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF7C3AED),
                                        unfocusedBorderColor = Color(0xFFE5E7EB)
                                    )
                                )

                                // Wallet/Account Select
                                Text("Source Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    accounts.forEach { acc ->
                                        val isSelected = selectedAccountId == acc.id
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF3F4F6))
                                                .clickable { selectedAccountId = acc.id }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                acc.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else Color(0xFF111827)
                                            )
                                        }
                                    }
                                }

                                if (tempTransactionType == TransactionType.TRANSFER) {
                                    Text("Destination Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        accounts.forEach { acc ->
                                            val isSelected = destinationAccountId == acc.id
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF3F4F6))
                                                    .clickable { destinationAccountId = acc.id }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    acc.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF111827)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Category Select
                                    Text("Category Select", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val listToUse = categories.filter { it.isIncome == (tempTransactionType == TransactionType.INCOME) }
                                        items(listToUse.size) { index ->
                                            val cat = listToUse[index]
                                            val isSelected = selectedCategoryId == cat.id
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF3F4F6))
                                                    .clickable { selectedCategoryId = cat.id }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    cat.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF111827)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Notes Input
                                OutlinedTextField(
                                    value = noteStr,
                                    onValueChange = { noteStr = it },
                                    label = { Text("Transaction details / Notes") },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(),
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
                                    val parsedAmt = amountStr.toDoubleOrNull()
                                    if (parsedAmt != null && parsedAmt > 0) {
                                        viewModel.insertTransaction(
                                            accountId = selectedAccountId,
                                            amount = parsedAmt,
                                            type = tempTransactionType,
                                            categoryId = if (tempTransactionType == TransactionType.TRANSFER) null else selectedCategoryId,
                                            note = noteStr,
                                            toAccountId = if (tempTransactionType == TransactionType.TRANSFER) destinationAccountId else null,
                                            transferFee = transferFeeStr.toDoubleOrNull(),
                                            paidByMemberId = null,
                                            splitType = null,
                                            splitDetailsJson = null,
                                            vendorName = null,
                                            invoiceNumber = null,
                                            taxRate = 0.0,
                                            taxAmount = null,
                                            isPaid = true
                                        )

                                        showGlobalAddDialog = false
                                        Toast.makeText(context, "Logged successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Invalid amount inputted", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Text("Log Entry", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showGlobalAddDialog = false }) {
                                Text("Cancel", color = Color(0xFFFF6B3D))
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }
                NavHost(
                    navController = navController,
                    startDestination = "dashboard",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTransactions = { navController.navigate("transactions") },
                            onNavigateToBudgets = { navController.navigate("budgets") }
                        )
                    }
                    composable("transactions") {
                        TransactionsScreen(viewModel = viewModel)
                    }
                    composable("budgets") {
                        val activeSpace by viewModel.activeSpace.collectAsState()
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sub-route tabs for Budgets, Savings, Accounts, and Categories
                            var personalSubTab by remember { mutableStateOf(0) }
                            ScrollableTabRow(selectedTabIndex = personalSubTab, modifier = Modifier.fillMaxWidth()) {
                                Tab(selected = personalSubTab == 0, onClick = { personalSubTab = 0 }) {
                                    Text("Budgets", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                }
                                Tab(selected = personalSubTab == 1, onClick = { personalSubTab = 1 }) {
                                    Text("Savings Goals", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                }
                                Tab(selected = personalSubTab == 2, onClick = { personalSubTab = 2 }) {
                                    Text("Accounts", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                }
                                Tab(selected = personalSubTab == 3, onClick = { personalSubTab = 3 }) {
                                    Text("Categories", modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                                }
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                when (personalSubTab) {
                                    0 -> BudgetsScreen(viewModel = viewModel)
                                    1 -> SavingsGoalsScreen(viewModel = viewModel)
                                    2 -> AccountsScreen(viewModel = viewModel)
                                    3 -> CategoriesScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                    composable("reports") {
                        ReportsScreen(viewModel = viewModel)
                    }
                    composable("family") {
                        FamilyScreen(viewModel = viewModel)
                    }
                    composable("business") {
                        BusinessScreen(viewModel = viewModel)
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateToAds = { navController.navigate("ads_showcase") }
                        )
                    }
                    composable("ads_showcase") {
                        GooglePlayAdsShowcaseScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.navigateUp() }
                        )
                    }
                }
            }
        }
    }
}
}
