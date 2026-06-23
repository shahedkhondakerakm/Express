package com.example.ui.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.theme.*
import com.example.ui.util.translated
import com.example.ui.util.LocalAppLanguage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBudgets: () -> Unit
) {
    val context = LocalContext.current
    val activeSpace by viewModel.activeSpace.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val spaces by viewModel.allSpaces.collectAsState()
    val isAdFreeModeActive by viewModel.isAdFreeModeActive.collectAsState()
    val rewardTokens by viewModel.adShowcaseRewardTokens.collectAsState()

    // Add Transaction Drawer State
    var showAddDialog by remember { mutableStateOf(false) }
    var showSpaceDialog by remember { mutableStateOf(false) }

    // Computations
    val systemCurrencySymbol = activeSpace?.currency ?: "$"
    val netWorth = viewModel.calculateNetWorth()
    
    // Optimize recompositions on transaction aggregations using remember blocks
    val monthlyExpTransactions = remember(transactions) {
        transactions.filter {
            it.type == TransactionType.EXPENSE &&
            it.date > getMonthStartTimestamp()
        }
    }
    val monthlyIncTransactions = remember(transactions) {
        transactions.filter {
            it.type == TransactionType.INCOME &&
            it.date > getMonthStartTimestamp()
        }
    }
    val currentMonthExpensesTotal = remember(monthlyExpTransactions) {
        monthlyExpTransactions.sumOf { it.amount }
    }
    val currentMonthIncomeTotal = remember(monthlyIncTransactions) {
        monthlyIncTransactions.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { showSpaceDialog = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (activeSpace?.type) {
                                SpaceType.FAMILY -> Icons.Default.Group
                                SpaceType.BUSINESS -> Icons.Default.Business
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeSpace?.name ?: "Personal Space",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showSpaceDialog = true }) {
                        Icon(imageVector = Icons.Default.SwapCalls, contentDescription = "Switch Space")
                    }
                },
                actions = {
                    Box {
                        var expandedSettings by remember { mutableStateOf(false) }
                        IconButton(onClick = { expandedSettings = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                        }
                        DropdownMenu(
                            expanded = expandedSettings,
                            onDismissRequest = { expandedSettings = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Space Window") },
                                onClick = {
                                    expandedSettings = false
                                    showSpaceDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.AddHome, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FC)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF6B3D),
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("submit_button")
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color(0xFFFF6B3D).copy(alpha = 0.4f),
                        spotColor = Color(0xFFFF6B3D).copy(alpha = 0.4f)
                    )
                    .padding(bottom = 16.dp),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ===== PREMIUM FINANCE HEADER =====
            val usernameState by viewModel.username.collectAsState()
            val displayName = usernameState ?: "User"
            val initials = displayName.take(2).uppercase()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile picture avatar with white background and elegant shadow
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(elevation = 4.dp, shape = CircleShape, clip = false, ambientColor = Color(0xFF111827).copy(alpha = 0.08f))
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { showSpaceDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF7C3AED),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Good Morning,".translated(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280),
                        )
                        Text(
                            text = displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                }

                // Row of notifications and space picker info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .clickable { showSpaceDialog = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (activeSpace?.type) {
                                    SpaceType.FAMILY -> Icons.Default.Group
                                    SpaceType.BUSINESS -> Icons.Default.Business
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = activeSpace?.name ?: "Personal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ===== PREMIUM DEBIT CARD-STYLE BALANCE BANNER WITH CUSTOM WAVE PATTERNS =====
            val premiumPurpleGradient = Brush.linearGradient(
                colors = listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF7C3AED).copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(premiumPurpleGradient)
                    .testTag("task_item_card")
            ) {
                // Vector overlapping sine-wave layout drawn behind
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.12f)
                ) {
                    val w = size.width
                    val h = size.height

                    // Wave 1
                    val path1 = Path().apply {
                        moveTo(0f, h * 0.45f)
                        cubicTo(
                            w * 0.3f, h * 0.2f,
                            w * 0.65f, h * 0.75f,
                            w, h * 0.4f
                        )
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path = path1, color = Color.White)

                    // Wave 2
                    val path2 = Path().apply {
                        moveTo(0f, h * 0.6f)
                        cubicTo(
                            w * 0.25f, h * 0.85f,
                            w * 0.7f, h * 0.4f,
                            w, h * 0.65f
                        )
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path = path2, color = Color.White)

                    // Wave 3
                    val path3 = Path().apply {
                        moveTo(0f, h * 0.75f)
                        cubicTo(
                            w * 0.35f, h * 0.6f,
                            w * 0.6f, h * 0.9f,
                            w, h * 0.7f
                        )
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path = path3, color = Color.White)
                }

                // Front Debit Card Artifacts
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Golden microchip vector overlay
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                                    )
                                )
                                .padding(4.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cw = size.width
                                val ch = size.height
                                drawLine(Color.Black.copy(alpha = 0.15f), Offset(cw * 0.3f, 0f), Offset(cw * 0.3f, ch), strokeWidth = 1.dp.toPx())
                                drawLine(Color.Black.copy(alpha = 0.15f), Offset(cw * 0.7f, 0f), Offset(cw * 0.7f, ch), strokeWidth = 1.dp.toPx())
                                drawLine(Color.Black.copy(alpha = 0.15f), Offset(0f, ch * 0.5f), Offset(cw, ch * 0.5f), strokeWidth = 1.dp.toPx())
                            }
                        }

                        // Premium text logo
                        Text(
                            text = "PREMIUM DEBIT".translated(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.75f),
                            letterSpacing = 1.5.sp
                        )
                    }

                    // Middle Section: Total Balance
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "TOTAL NET WORTH".translated(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$systemCurrencySymbol${String.format("%,.2f", netWorth)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Bottom Section: Space Allocation Name, Masked Digit, Mastercard styled circles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = (activeSpace?.name ?: "Personal Space").uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "•••• •••• •••• 8820",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f),
                                letterSpacing = 1.5.sp
                            )
                        }

                        // Contactless + Two-Color Overlapping Network Rings
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer(rotationZ = 90f)
                            )
                            
                            Box(contentAlignment = Alignment.Center) {
                                Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF7C3AED).copy(alpha = 0.85f))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF6B3D).copy(alpha = 0.85f))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AD-FREE TOKEN SUPPRESSION SYSTEM INTEGRATION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.04f))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(
                        width = 1.6.dp,
                        brush = Brush.horizontalGradient(
                            if (isAdFreeModeActive) listOf(Color(0xFF10B981), Color(0xFF059669))
                            else listOf(Color(0xFF7C3AED), Color(0xFFFF6B3D))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAdFreeModeActive) Color(0xFF10B981).copy(alpha = 0.1f)
                                    else Color(0xFF7C3AED).copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAdFreeModeActive) Icons.Default.AllInclusive else Icons.Default.Stars,
                                contentDescription = null,
                                tint = if (isAdFreeModeActive) Color(0xFF10B981) else Color(0xFF7C3AED),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isAdFreeModeActive) "Ad-Free Premium Sandbox".translated() else "Support Expanager Free".translated(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAdFreeModeActive) "Token suppression active - enjoy neat sheets!".translated() else "Spend 1 Token ($rewardTokens available) to hide simulated ads!".translated(),
                                fontSize = 11.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                    if (!isAdFreeModeActive) {
                        Button(
                            onClick = {
                                if (viewModel.activateAdFree24Hours()) {
                                    Toast.makeText(context, "Premium Active! Ad-Free Suppression Mode Enabled.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Insufficient Tokens! Watch rewarding ads inside Ads Showcase Hub.", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Use 1 Token".translated(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "ACTIVE".translated(),
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== QUICK STATS BENTO-STYLE GRID =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Income Bento Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .clickable { onNavigateToTransactions() }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Income",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MONEY IN".translated(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$systemCurrencySymbol${String.format("%,.0f", currentMonthIncomeTotal)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827),
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }
                }

                // Monthly Expense Bento Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .clickable { onNavigateToTransactions() }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF6B3D).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Expense",
                                tint = Color(0xFFFF6B3D),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "MONEY OUT".translated(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$systemCurrencySymbol${String.format("%,.0f", currentMonthExpensesTotal)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF111827),
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }
                }
            }

            // ===== PIE CHART SPENDING SECTION =====
            if (monthlyExpTransactions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Spending Category Share (Month)".translated(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        val categorySpending = monthlyExpTransactions.groupBy { it.categoryId }
                        val slices = categorySpending.map { (catId, txns) ->
                            val cat = categories.find { it.id == catId }
                            val totalAmt = txns.sumOf { it.amount }
                            PieSlice(
                                label = cat?.name ?: "Other",
                                value = totalAmt,
                                color = Color(cat?.color?.toLong() ?: 0xFF90A4AE)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AnimatedPieChart(
                                slices = slices,
                                modifier = Modifier.size(110.dp),
                                thickness = 14.dp
                            )
                            
                            Column {
                                slices.take(4).forEach { slice ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(slice.color))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${slice.label}: $systemCurrencySymbol${slice.value.toInt()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ===== TOP BUDGETS CARD =====
            if (budgets.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.05f))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Budget Progress".translated(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF111827)
                            )
                            TextButton(onClick = onNavigateToBudgets) {
                                Text(
                                    text = "See All".translated(),
                                    fontSize = 12.sp,
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        budgets.take(3).forEach { budget ->
                            val cat = categories.find { it.id == budget.categoryId }
                            val spent = transactions.filter { it.categoryId == budget.categoryId && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                            val progress = if (budget.limitAmount > 0) (spent / budget.limitAmount).toFloat() else 0f
                            
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat?.name ?: "General Budget",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        text = "$systemCurrencySymbol${spent.toInt()} / $systemCurrencySymbol${budget.limitAmount.toInt()}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                    color = when {
                                        progress >= 1.0f -> Color(0xFFEF4444)
                                        progress >= 0.8f -> Color(0xFFFBBF24)
                                        else -> Color(0xFF10B981)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ===== PREMIUM RECENT TRANSACTION LIST CONTAINER =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFF111827).copy(alpha = 0.05f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activities".translated(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                        TextButton(onClick = onNavigateToTransactions) {
                            Text(
                                "See All".translated(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    val activeTxns = transactions.take(5)
                    if (activeTxns.isEmpty()) {
                        AppEmptyState(
                            title = "No recent records".translated(),
                            tip = "Tap the '+' floating action button below to log your first transaction.".translated(),
                            icon = Icons.Default.ReceiptLong
                        )
                    } else {
                        activeTxns.forEach { txn ->
                            val cat = categories.find { it.id == txn.categoryId }
                            val acc = accounts.find { it.id == txn.accountId }
                            val categoryIcon = cat?.icon ?: "Receipt"
                            val colorCode = cat?.color?.toLong() ?: 0xFF90A4AE

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(colorCode).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = IconLoader.getIcon(categoryIcon),
                                            contentDescription = null,
                                            tint = Color(colorCode),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = txn.note.ifEmpty { cat?.name ?: "Transfer Wallet" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF111827)
                                        )
                                        Text(
                                            text = "${acc?.name ?: "Asset"} • ${formatDate(txn.date)}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6B7280),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Text(
                                    text = if (txn.type == TransactionType.INCOME) "+$systemCurrencySymbol${String.format("%,.1f", txn.amount)}"
                                           else "-$systemCurrencySymbol${String.format("%,.1f", txn.amount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (txn.type == TransactionType.INCOME) Color(0xFF10B981) else Color(0xFFFF6B3D)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFEDF2F7), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SPACE SWITCHER / ADD SPACE MODAL
    // ==========================================
    if (showSpaceDialog) {
        var isCreatingSpace by remember { mutableStateOf(false) }
        var spaceName by remember { mutableStateOf("") }
        var spaceTypeSelected by remember { mutableStateOf(SpaceType.PERSONAL) }
        var spaceCurrencySelected by remember { mutableStateOf("USD") }

        AlertDialog(
            onDismissRequest = { showSpaceDialog = false },
            title = { Text(if (isCreatingSpace) "Create New Space" else "Active Space Switcher", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isCreatingSpace) {
                        Text("Switch between multiple distinct isolations (Personal, Family ledger pools, or Business accounts):", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        spaces.forEach { sp ->
                            val isSelected = sp.id == activeSpace?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        viewModel.switchActiveSpace(sp.id)
                                        showSpaceDialog = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when(sp.type) {
                                            SpaceType.FAMILY -> Icons.Default.Group
                                            SpaceType.BUSINESS -> Icons.Default.Business
                                            else -> Icons.Default.Person
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(sp.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("${sp.type} • ${sp.currency}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Button(
                            onClick = { isCreatingSpace = true },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Space Profile")
                        }
                    } else {
                        // Create Space Form
                        OutlinedTextField(
                            value = spaceName,
                            onValueChange = { spaceName = it },
                            label = { Text("Space Title Name") },
                            placeholder = { Text("e.g. My Startup or Family Budget") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Space Category Mode Type:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SpaceType.values().forEach { type ->
                                val isSelected = spaceTypeSelected == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { spaceTypeSelected = type },
                                    label = { Text(type.name, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = spaceCurrencySelected,
                            onValueChange = { spaceCurrencySelected = it },
                            label = { Text("Space Base Currency Code") },
                            placeholder = { Text("e.g. USD, EUR, BDT") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (isCreatingSpace) {
                    Button(
                        onClick = {
                            if (spaceName.isNotEmpty()) {
                                viewModel.createSpace(spaceName, spaceTypeSelected, spaceCurrencySelected)
                                isCreatingSpace = false
                                showSpaceDialog = false
                                Toast.makeText(context, "Space Profile Created & Selected!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Create")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (isCreatingSpace) isCreatingSpace = false
                        else showSpaceDialog = false
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // ==========================================
    // QUICK ADD TRANSACTION BOTTOM DIALOGUE / SHEET
    // ==========================================
    if (showAddDialog) {
        var amountStr by remember { mutableStateOf("") }
        var tempTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
        var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
        var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
        var noteStr by remember { mutableStateOf("") }
        
        // Trans-Specific
        var destinationAccountId by remember { mutableStateOf(accounts.firstOrNull { it.id != selectedAccountId }?.id ?: "") }
        var transferFeeStr by remember { mutableStateOf("") }

        // Family Specific (Phase 2 Add-ons)
        var splitFamilyPayerId by remember { mutableStateOf(viewModel.familyMembers.value.firstOrNull()?.id ?: "") }
        var isSplittingBill by remember { mutableStateOf(false) }
        var splitTypeSelected by remember { mutableStateOf("EQUAL") }

        // Business Specific (Phase 3 Add-ons)
        var vendorText by remember { mutableStateOf("") }
        var invoiceNumText by remember { mutableStateOf("") }
        var taxRateText by remember { mutableStateOf("0") }
        var isPaidCheckboxState by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Transaction Entry", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type Selection Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TransactionType.values().forEach { t ->
                            val isSelected = tempTransactionType == t
                            val color = when (t) {
                                TransactionType.INCOME -> Color(0xFF4CAF50)
                                TransactionType.EXPENSE -> Color(0xFFE53935)
                                TransactionType.TRANSFER -> Color(0xFF2196F3)
                            }
                            Button(
                                onClick = { tempTransactionType = t },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(t.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Numeric Amount keyboard first
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Ledger Amount Float") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Text(systemCurrencySymbol, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_button_amount_input"),
                        singleLine = true
                    )

                    // Account selector
                    Text("Select Account Asset:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                            accounts.forEach { acc ->
                                val isSelected = acc.id == selectedAccountId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text(acc.name, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    if (tempTransactionType == TransactionType.TRANSFER) {
                        // Destination account selection
                        Text("Destination Transfer Account Target:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                                accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                    val isSelected = acc.id == destinationAccountId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { destinationAccountId = acc.id },
                                        label = { Text(acc.name, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = transferFeeStr,
                            onValueChange = { transferFeeStr = it },
                            label = { Text("Transfer Processing Fee (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Category Selector chip rows
                        Text("Choose Subcategory Tag:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.filter { it.isIncome == (tempTransactionType == TransactionType.INCOME) }.forEach { cat ->
                                    val isSelected = cat.id == selectedCategoryId
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategoryId = cat.id },
                                        label = { Text(cat.name, fontSize = 10.sp) },
                                        leadingIcon = { Icon(IconLoader.getIcon(cat.icon), contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = { noteStr = it },
                        label = { Text("Memo / Explanation Notes") },
                        placeholder = { Text("e.g. Weekly Grocery or Lunch bill") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // PHASE 2 - FAMILY MEMBERS MULTIPLE JOIN SPLITS DIALOGUE
                    if (activeSpace?.type == SpaceType.FAMILY && tempTransactionType == TransactionType.EXPENSE && viewModel.familyMembers.value.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Split Expense Among Members", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Switch(checked = isSplittingBill, onCheckedChange = { isSplittingBill = it })
                        }
                        
                        if (isSplittingBill) {
                            Text("Paid By Family Member:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    viewModel.familyMembers.value.forEach { mem ->
                                        val isSelected = mem.id == splitFamilyPayerId
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { splitFamilyPayerId = mem.id },
                                            label = { Text(mem.name, fontSize = 11.sp) }
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("EQUAL", "PERCENTAGE").forEach { type ->
                                    val isSelected = splitTypeSelected == type
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { splitTypeSelected = type },
                                        label = { Text(type, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // PHASE 3 - BUSINESS METADATA PAYABLES
                    if (activeSpace?.type == SpaceType.BUSINESS) {
                        Text("Business Purchase Invoicing (Phase 3):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        
                        OutlinedTextField(
                            value = vendorText,
                            onValueChange = { vendorText = it },
                            label = { Text("Vendor Vendor Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = invoiceNumText,
                                onValueChange = { invoiceNumText = it },
                                label = { Text("Invoice No.") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = taxRateText,
                                onValueChange = { taxRateText = it },
                                label = { Text("Tax %") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { isPaidCheckboxState = !isPaidCheckboxState }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mark Bill Already Fully Paid", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Checkbox(checked = isPaidCheckboxState, onCheckedChange = { isPaidCheckboxState = it })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedAmt = amountStr.toDoubleOrNull()
                        if (parsedAmt != null && parsedAmt > 0) {
                            var splitJsonResult: String? = null
                            if (isSplittingBill && activeSpace?.type == SpaceType.FAMILY) {
                                // build serialized equal split details json array
                                val targetMembers = viewModel.familyMembers.value
                                if (targetMembers.isNotEmpty()) {
                                    val equalShare = parsedAmt / targetMembers.size
                                    val arr = JSONArray()
                                    targetMembers.forEach { m ->
                                        val obj = JSONObject()
                                        obj.put("memberId", m.id)
                                        obj.put("shareAmount", equalShare)
                                        arr.put(obj)
                                    }
                                    splitJsonResult = arr.toString()
                                }
                            }

                            val tRate = taxRateText.toDoubleOrNull() ?: 0.0
                            val tAmount = if (tRate > 0) parsedAmt * (tRate / 100.0) else null

                            viewModel.insertTransaction(
                                accountId = selectedAccountId,
                                amount = parsedAmt,
                                type = tempTransactionType,
                                categoryId = if (tempTransactionType == TransactionType.TRANSFER) null else selectedCategoryId,
                                note = noteStr,
                                toAccountId = if (tempTransactionType == TransactionType.TRANSFER) destinationAccountId else null,
                                transferFee = transferFeeStr.toDoubleOrNull(),
                                paidByMemberId = if (isSplittingBill) splitFamilyPayerId else null,
                                splitType = if (isSplittingBill) splitTypeSelected else null,
                                splitDetailsJson = splitJsonResult,
                                vendorName = vendorText.ifEmpty { null },
                                invoiceNumber = invoiceNumText.ifEmpty { null },
                                taxRate = tRate,
                                taxAmount = tAmount,
                                isPaid = isPaidCheckboxState
                            )
                            
                            showAddDialog = false
                            Toast.makeText(context, "Transaction log registered!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("submit_onboarding_button")
                ) {
                    Text("Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper methods for Dashboard Dates
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getMonthStartTimestamp(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    return cal.timeInMillis
}
