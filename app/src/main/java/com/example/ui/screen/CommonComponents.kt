package com.example.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountType
import com.example.data.model.SpaceType
import com.example.data.model.TransactionType

// = ascension helper for visual icons across tabs
object IconLoader {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "Restaurant", "Food" -> Icons.Default.Restaurant
            "DirectionsCar", "Transport" -> Icons.Default.DirectionsCar
            "LocalMall", "Shopping" -> Icons.Default.LocalMall
            "Receipt", "Bills" -> Icons.Default.Receipt
            "LocalHospital", "Health" -> Icons.Default.LocalHospital
            "SportsEsports", "Game" -> Icons.Default.SportsEsports
            "School", "Education" -> Icons.Default.School
            "Home", "Housing" -> Icons.Default.Home
            "Face", "Personal" -> Icons.Default.Face
            "AttachMoney", "Income" -> Icons.Default.AttachMoney
            "Work", "Salary" -> Icons.Default.Work
            "Payments", "Payroll" -> Icons.Default.Payments
            "LocalGasStation", "Fuel" -> Icons.Default.LocalGasStation
            "Inventory2", "Office" -> Icons.Default.Inventory2
            "AccountBalance", "Tax" -> Icons.Default.AccountBalance
            "ShoppingBasket", "Basket" -> Icons.Default.ShoppingBasket
            "ChildCare", "Kids" -> Icons.Default.ChildCare
            "Power", "Utilities" -> Icons.Default.Power
            "TrendingUp" -> Icons.Default.TrendingUp
            "CardMembership" -> Icons.Default.CardMembership
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "Payment" -> Icons.Default.Payment
            "Smartphone" -> Icons.Default.Smartphone
            "Group", "Family" -> Icons.Default.Group
            "Business", "Briefcase" -> Icons.Default.Business
            "Person" -> Icons.Default.Person
            else -> Icons.Default.Star
        }
    }

    val availableIcons = listOf(
        "Restaurant", "DirectionsCar", "LocalMall", "Receipt", "LocalHospital",
        "SportsEsports", "School", "Home", "Face", "AttachMoney", "Payments",
        "LocalGasStation", "Inventory2", "AccountBalance", "ShoppingBasket", "ChildCare"
    )
}

@Composable
fun AppEmptyState(
    title: String,
    tip: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = tip,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = actionText)
            }
        }
    }
}

// Custom Draw Canvas Pie Chart
@Composable
fun AnimatedPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    thickness: Dp = 18.dp
) {
    val total = slices.sumOf { it.value }
    if (total == 0.0) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No transactions in selected period", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
        return
    }

    var startAngle = 270f
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height)
        val rectSize = Size(diameter, diameter)
        val topLeftOffset = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

        slices.forEach { slice ->
            val sweepAngle = ((slice.value / total) * 360f).toFloat()
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round),
                size = rectSize,
                topLeft = topLeftOffset
            )
            startAngle += sweepAngle
        }
    }
}

data class PieSlice(
    val label: String,
    val value: Double,
    val color: Color
)

// Custom Canvas Bar Chart for Income vs Expenses
@Composable
fun IncomeExpenseBarChart(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "$"
) {
    val maxVal = maxOf(income, expense, 100.0)
    val incomeHeightProgress = animateFloatAsState(targetValue = (income / maxVal).toFloat(), animationSpec = tween(800), label = "")
    val expenseHeightProgress = animateFloatAsState(targetValue = (expense / maxVal).toFloat(), animationSpec = tween(800), label = "")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Income Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$currencySymbol${income.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(incomeHeightProgress.value)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF62F784), Color(0xFF00D632))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Income", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            // Expense Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$currencySymbol${expense.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC62828))
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .fillMaxHeight(expenseHeightProgress.value)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF8A80), Color(0xFFC62828))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Expense", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// Line trends chart
@Composable
fun NetWorthLineChart(
    balances: List<Double>,
    modifier: Modifier = Modifier
) {
    if (balances.size < 2) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Insufficent historical data to plot trend line", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        return
    }

    val max = balances.maxOrNull() ?: 1.0
    val min = balances.minOrNull() ?: 0.0
    val range = if (max == min) 1.0 else (max - min)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val points = mutableListOf<Offset>()

        balances.forEachIndexed { index, balance ->
            val x = (index.toFloat() / (balances.size - 1)) * width
            val y = height - (((balance - min) / range) * height).toFloat()
            points.add(Offset(x, y))
        }

        // Draw connecting curves/lines
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF00D632),
                start = points[i],
                end = points[i+1],
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }

        // Draw points nodes
        points.forEach { pt ->
            drawCircle(
                color = Color(0xFF003F15),
                radius = 10f,
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = pt
            )
        }
    }
}
