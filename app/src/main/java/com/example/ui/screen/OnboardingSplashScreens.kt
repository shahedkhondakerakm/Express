package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// --- PREMIUM PALETTE ---
val BrandPurple = Color(0xFF7C3AED)
val BrandPurpleSecondary = Color(0xFF9333EA)
val BrandOrange = Color(0xFFFF6B3D)
val BrandBackground = Color(0xFFF8F9FC)
val BrandDarkIndigo = Color(0xFF1F2340)

enum class SplashPhase {
    INTRO_LOGO,
    SPLASH_1,
    SPLASH_2,
    SPLASH_3
}

@Composable
fun SplashOnboardingFlow(
    onFinished: () -> Unit
) {
    var currentPhase by remember { mutableStateOf(SplashPhase.INTRO_LOGO) }

    // State of page elements to orchestrate sequential visual reveals
    var logoProgress by remember { mutableStateOf(0f) }
    var logoGlowAlpha by remember { mutableStateOf(0f) }
    var logoRingProgress by remember { mutableStateOf(0f) }

    // Page phase navigation with smooth fade animation
    AnimatedContent(
        targetState = currentPhase,
        transitionSpec = {
            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
        },
        label = "SplashPageTransition"
    ) { phase ->
        when (phase) {
            SplashPhase.INTRO_LOGO -> {
                LogoIntroScreen(
                    onAnimationComplete = {
                        currentPhase = SplashPhase.SPLASH_1
                    }
                )
            }
            SplashPhase.SPLASH_1 -> {
                SplashPage1(
                    onNext = { currentPhase = SplashPhase.SPLASH_2 },
                    onSkip = onFinished
                )
            }
            SplashPhase.SPLASH_2 -> {
                SplashPage2(
                    onNext = { currentPhase = SplashPhase.SPLASH_3 },
                    onSkip = onFinished
                )
            }
            SplashPhase.SPLASH_3 -> {
                SplashPage3(
                    onFinished = onFinished,
                    onSkip = onFinished
                )
            }
        }
    }
}

// ==========================================
// 1. LOGO INTRO ANIMATION SCREEN
// ==========================================
@Composable
fun LogoIntroScreen(onAnimationComplete: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val ringAngle = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Scale up logo with beautiful spring overshooting bounce
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        // Step 2: Concurrent Glow expand & Sweep outer ring
        glowAlpha.animateTo(0.15f, animationSpec = tween(600, easing = LinearOutSlowInEasing))
        textAlpha.animateTo(1f, animationSpec = tween(400))
        
        ringAngle.animateTo(360f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        
        delay(600) // Sustain premium moment
        
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Purple soft radial background light leakage (ambient glow)
        Canvas(
            modifier = Modifier
                .size(400.dp)
                .alpha(glowAlpha.value)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BrandPurple, Color.Transparent),
                    center = center,
                    radius = size.width * 0.45f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                // Vector Orange active ring circling around the wallet
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = BrandOrange,
                        startAngle = -90f,
                        sweepAngle = ringAngle.value,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner premium Glass Wallet logo
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(26.dp), spotColor = BrandPurple.copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandPurple, BrandPurpleSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Minimal Wallet card slot lines + golden upward dynamic arrow overlay
                    Canvas(modifier = Modifier.fillMaxSize().padding(22.dp)) {
                        val w = size.width
                        val h = size.height

                        // Wallet shape cutout styling lines
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(0f, h * 0.3f),
                            end = Offset(w * 0.65f, h * 0.3f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(0f, h * 0.55f),
                            end = Offset(w * 0.45f, h * 0.55f),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Golden Orange arrow charging upwards out of the wallet pocket
                        val arrowPath = Path().apply {
                            moveTo(w * 0.5f, h)
                            lineTo(w * 0.5f, h * 0.2f)
                            moveTo(w * 0.25f, h * 0.4f)
                            lineTo(w * 0.5f, h * 0.12f)
                            lineTo(w * 0.75f, h * 0.4f)
                        }
                        drawPath(
                            path = arrowPath,
                            color = BrandOrange,
                            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Finance Pro",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandDarkIndigo,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "FUTURE OF WEALTH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrandPurple,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}


// ==========================================
// 2. SPLASH SCREEN 1: "Track Every Dollar"
// ==========================================
@Composable
fun SplashPage1(onNext: () -> Unit, onSkip: () -> Unit) {
    // Sequence state variables
    var showSmartphone by remember { mutableStateOf(false) }
    var graphAnimateProgress by remember { mutableStateOf(0f) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        showSmartphone = true
        delay(400)
        // Draw bars and scale graphs
        animate(0f, 1f, animationSpec = tween(1100, easing = FastOutSlowInEasing)) { value, _ ->
            graphAnimateProgress = value
        }
        showButton = true
    }

    // Infinite gentle organic float transitions for floating assets
    val infiniteTransition = rememberInfiniteTransition(label = "FloatInfinity")
    val floatY1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY1"
    )
    val floatY2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY2"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Skip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .clickable { onSkip() }
                        .padding(8.dp)
                )
            }

            // Visual Center Illustration Area
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Background Ambient Aura Soft Purple Glow
                Canvas(modifier = Modifier.size(280.dp).alpha(0.12f)) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(BrandPurple, Color.Transparent)),
                        radius = size.width * 0.5f
                    )
                }

                // 2.1 SMARTPHONE ILLUSTRATION WITH BALANCE CONTAINER
                androidx.compose.animation.AnimatedVisibility(
                    visible = showSmartphone,
                    enter = slideInVertically(
                        initialOffsetY = { 350 },
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(340.dp)
                            .shadow(24.dp, shape = RoundedCornerShape(32.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.12f))
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        // Smartphone inner hardware screen container
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Phone top notch
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp, 10.dp)
                                        .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                        .background(Color.Black)
                                )
                            }

                            // A. Balance Card (Mini widget)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(BrandPurple, BrandPurpleSecondary)
                                        )
                                    )
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                                    Text("SAVINGS BALANCE", fontSize = 7.sp, color = Color.White.copy(0.7f), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text("$48,720.00", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("•••• 4209", fontSize = 7.sp, color = Color.White.copy(0.6f), fontWeight = FontWeight.Medium)
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandOrange))
                                    }
                                }
                            }

                            // B. Mini-Graph Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("Expenses Overview", fontSize = 8.sp, color = BrandDarkIndigo, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        val barHeights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f, 0.8f)
                                        barHeights.forEach { targetHeight ->
                                            Box(
                                                modifier = Modifier
                                                    .width(14.dp)
                                                    .fillMaxHeight(targetHeight * graphAnimateProgress)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (targetHeight > 0.7f) BrandOrange else BrandPurple
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // C. Transaction List Mini container
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Recent Receipts", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                listOf(
                                    Triple("Starbucks Cafe", "-$12.50", "Coffee & Dining"),
                                    Triple("Spotify Sub", "-$14.99", "Premium Entertainment")
                                ).forEach { (name, amount, cat) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(BrandPurple.copy(0.12f)))
                                            Column {
                                                Text(name, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = BrandDarkIndigo)
                                                Text(cat, fontSize = 5.sp, color = Color.Gray)
                                            }
                                        }
                                        Text(amount, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2.2 FLOATING COMPONENT ORNAMENTS
                // Ornament 1: Dollar Round Badge Floating Left
                Box(
                    modifier = Modifier
                        .offset(x = (-110).dp, y = (-70).dp + floatY1.dp)
                        .size(46.dp)
                        .shadow(12.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(BrandOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }

                // Ornament 2: Premium credit card styled float floating right
                Box(
                    modifier = Modifier
                        .offset(x = 110.dp, y = 30.dp + floatY2.dp)
                        .size(height = 54.dp, width = 86.dp)
                        .graphicsLayer(rotationZ = 12f)
                        .shadow(16.dp, shape = RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(BrandDarkIndigo, Color(0xFF2E1065))
                            )
                        )
                        .padding(6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp, 8.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFF59E0B)))
                            Text("PRO", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("•••• 8820", fontSize = 8.sp, color = Color.White.copy(0.8f))
                    }
                }

                // Ornament 3: Large Growth Arrow Floating top-right direction
                Box(
                    modifier = Modifier
                        .offset(x = 90.dp, y = (-120).dp + floatY1.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Typography description block
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Track Every Dollar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandDarkIndigo,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Monitor income, expenses, and savings in one powerful dashboard.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom controls: Indicators and Action Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator: ● ○ ○
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandOrange))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
                }

                // Active Button
                AnimatedVisibility(
                    visible = showButton,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.72f)) + fadeIn()
                ) {
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .width(110.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = BrandPurple.copy(0.42f),
                                spotColor = BrandPurple.copy(0.42f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(BrandPurple, BrandPurpleSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 3. SPLASH SCREEN 2: "Smart Budget Planning"
// ==========================================
@Composable
fun SplashPage2(onNext: () -> Unit, onSkip: () -> Unit) {
    var budgetRingSweep by remember { mutableStateOf(0f) }
    var barsPercentage by remember { mutableStateOf(0f) }
    var showButton by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        showCard = true
        delay(50)
        // Set sweep and fill values synchronously
        animate(0f, 275f, animationSpec = tween(1200, easing = EaseOutCubic)) { value, _ ->
            budgetRingSweep = value
        }
        animate(0f, 1f, animationSpec = tween(800, easing = EaseInOutSine)) { valPercent, _ ->
            barsPercentage = valPercent
        }
        showButton = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Page2Infinity")
    val floatTranslation by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftPage2"
    )

    // Continuous soft pie chart rotation factor
    val rotatePieAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotatePieChart"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Skip button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Skip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .clickable { onSkip() }
                        .padding(8.dp)
                )
            }

            // Visual Center: Glassmorphism layout Dashboard with progress ring & progress bars
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Glow Background
                Canvas(modifier = Modifier.size(310.dp).alpha(0.08f)) {
                    drawCircle(brush = Brush.radialGradient(listOf(BrandOrange, Color.Transparent)))
                }

                // BEAUTIFUL CENTRAL FINANCE BUDGET HUB
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCard,
                    enter = slideInVertically(
                        initialOffsetY = { 350 },
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .height(230.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.08f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(18.dp)
                    ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: Budget Progress Ring Draw Area
                        Column(
                            modifier = Modifier.weight(1.1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Double arc progress rings drawing custom canvas
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val rSize = size.width
                                    val strokeW = 10.dp.toPx()

                                    // Inner light track
                                    drawCircle(
                                        color = Color(0xFFE5E7EB),
                                        style = Stroke(width = strokeW)
                                    )

                                    // Purple sweeping progress
                                    drawArc(
                                        color = BrandPurple,
                                        startAngle = -90f,
                                        sweepAngle = budgetRingSweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                    )
                                }

                                // Center textual progress indicator
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(budgetRingSweep / 3.6f).toInt()}%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandDarkIndigo
                                    )
                                    Text(
                                        text = "SPENT",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }
                        }

                        // Right Column: Categories and limits progress bars
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Budget Categories", fontSize = 11.sp, color = BrandDarkIndigo, fontWeight = FontWeight.Bold)

                            listOf(
                                Pair("Dining Out", 0.85f),
                                Pair("Rent & Taxes", 0.45f),
                                Pair("Gadgets Store", 0.65f)
                            ).forEach { (catName, progressVal) ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = catName, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = BrandDarkIndigo)
                                        Text(text = "${(progressVal * 100).toInt()}%", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Custom visual progress bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE5E7EB))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction = progressVal * barsPercentage)
                                                .clip(CircleShape)
                                                .background(
                                                    if (progressVal > 0.7f) BrandOrange else BrandPurpleSecondary
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                }

                // 2.3 FLOATING SYSTEM OF ICONS GENTLY WOBBLING
                // 1. Target board Floating top-right with wobble translation
                Box(
                    modifier = Modifier
                        .offset(x = 110.dp, y = (-75).dp - floatTranslation.dp)
                        .size(44.dp)
                        .shadow(8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(BrandOrange.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Adjust, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(22.dp))
                    }
                }

                // 2. Leather wallet floating bottom-left
                Box(
                    modifier = Modifier
                        .offset(x = (-110).dp, y = 70.dp + floatTranslation.dp)
                        .size(44.dp)
                        .shadow(8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(BrandPurple.copy(0.11f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(20.dp))
                    }
                }

                // 3. Floating rotating pie-chart vector top-left
                Box(
                    modifier = Modifier
                        .offset(x = (-105).dp, y = (-85).dp + floatTranslation.dp)
                        .size(42.dp)
                        .graphicsLayer(rotationZ = rotatePieAngle)
                        .shadow(10.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(7.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = BrandPurple,
                            startAngle = 0f,
                            sweepAngle = 230f,
                            useCenter = true
                        )
                        drawArc(
                            color = BrandOrange,
                            startAngle = 230f,
                            sweepAngle = 130f,
                            useCenter = true
                        )
                    }
                }
            }

            // Typography block
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Smart Budget Planning",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandDarkIndigo,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set budgets, control spending, and achieve your financial goals.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom controls: Indicators and Action Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator: ○ ● ○
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandOrange))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
                }

                // Next Button
                AnimatedVisibility(
                    visible = showButton,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.72f)) + fadeIn()
                ) {
                    Button(
                        onClick = onNext,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .width(110.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = BrandPurple.copy(0.42f),
                                spotColor = BrandPurple.copy(0.42f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(BrandPurple, BrandPurpleSecondary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. SPLASH SCREEN 3: "Grow Your Financial Future"
// ==========================================
@Composable
fun SplashPage3(onFinished: () -> Unit, onSkip: () -> Unit) {
    var graphDrawState by remember { mutableStateOf(0f) }
    var pieExpandState by remember { mutableStateOf(0f) }
    var showSuccessCard by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showCard = true
        delay(50)
        animate(0f, 1f, animationSpec = tween(900, easing = FastOutSlowInEasing)) { drawVal, _ ->
            graphDrawState = drawVal
        }
        animate(0f, 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { animVal, _ ->
            pieExpandState = animVal
        }
        showSuccessCard = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Page3Infinity")
    val floatMovementY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftPage3"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground
    ) { progressInPaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(progressInPaddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Space
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Skip",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .clickable { onSkip() }
                        .padding(8.dp)
                )
            }

            // Visual Center: Premium Analytics Dashboard & giant colorful gradient arrows
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Success glow particle aura behind
                Canvas(
                    modifier = Modifier
                        .size(310.dp)
                        .scale(pieExpandState)
                        .alpha(0.12f)
                ) {
                    drawCircle(brush = Brush.radialGradient(listOf(BrandPurple, Color.Transparent)))
                }

                // BEAUTIFUL PREMIUM ANALYTICS BOARD WITH LINE GRAPH
                androidx.compose.animation.AnimatedVisibility(
                    visible = showCard,
                    enter = slideInVertically(
                        initialOffsetY = { 350 },
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .width(280.dp)
                            .height(230.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color(0xFF111827).copy(alpha = 0.08f))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(18.dp)
                    ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("FINANCIAL FORECAST", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("Wealth Growth Plan", fontSize = 13.sp, color = BrandDarkIndigo, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(BrandPurple.copy(0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Curved smooth line graph rendered inside canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val points = listOf(
                                Offset(w * 0.0f, h * 0.85f),
                                Offset(w * 0.2f, h * 0.65f),
                                Offset(w * 0.4f, h * 0.75f),
                                Offset(w * 0.6f, h * 0.4f),
                                Offset(w * 0.8f, h * 0.5f),
                                Offset(w * 1.0f, h * 0.15f)
                            )

                            // Generate smooth cubic Spline graph path
                            val path = Path()
                            if (points.isNotEmpty()) {
                                path.moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val ctrlX1 = prev.x + (curr.x - prev.x) / 2
                                    val ctrlY1 = prev.y
                                    val ctrlX2 = prev.x + (curr.x - prev.x) / 2
                                    val ctrlY2 = curr.y
                                    path.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, curr.x, curr.y)
                                }
                            }

                            // Clip line drawing based on animation state
                            clipRect(right = w * graphDrawState) {
                                // Draw beautiful vertical gradient fill under line path
                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(w, h)
                                    lineTo(0f, h)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(BrandPurple.copy(alpha = 0.22f), Color.Transparent)
                                    )
                                )

                                // Draw main line stroke
                                drawPath(
                                    path = path,
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(BrandPurple, BrandPurpleSecondary, BrandOrange)
                                    ),
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Draw peak control point circles
                                points.forEach { pt ->
                                    drawCircle(BrandOrange, radius = 4.dp.toPx(), center = pt)
                                    drawCircle(Color.White, radius = 2.dp.toPx(), center = pt)
                                }
                            }
                        }

                        // Bottom summary details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val miniStateColors = listOf(BrandPurple, BrandOrange)
                                miniStateColors.forEach { c ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(c))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Savings", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text("+$12,480 / year (+18.4%)", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                }

                // 2.3 OVERLAPPING LARGE DYNAMIC GRADIENT GROWTH ARROW
                androidx.compose.animation.AnimatedVisibility(
                    visible = showSuccessCard,
                    enter = slideInVertically(
                        initialOffsetY = { 200 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.85f)
                    ) + fadeIn()
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = 90.dp, y = (45).dp + floatMovementY.dp)
                            .shadow(20.dp, shape = RoundedCornerShape(16.dp), spotColor = BrandOrange.copy(0.3f))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BrandPurpleSecondary, BrandOrange)
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            Text("Growth Goal Active", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Typography content area
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Grow Your Financial Future",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandDarkIndigo,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Powerful analytics and insights to help you build wealth.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar indicator & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress Indicator: ○ ○ ●
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandOrange))
                }

                // Call to actions trigger: Purple Gradient "Get Started" with premium glow shadow
                Button(
                    onClick = onFinished,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .width(135.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = BrandPurple.copy(0.42f),
                            spotColor = BrandPurple.copy(0.42f)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(BrandPurple, BrandPurpleSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
