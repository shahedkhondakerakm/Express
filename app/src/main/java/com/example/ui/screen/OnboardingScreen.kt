package com.example.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.util.translated
import com.example.ui.util.LocalAppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: ExpenseViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Step 1: Privacy Explainer
    // Step 2: Name & Language
    // Step 3: Default Currency
    // Step 4: PIN Security Configuration
    
    var name by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var selectedCurrency by remember { mutableStateOf("USD") }
    var initialBalanceStr by remember { mutableStateOf("500") }
    
    var useBiometrics by remember { mutableStateOf(false) }
    var setupPin by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }

    var showSplashFlow by remember { mutableStateOf(true) }

    if (showSplashFlow) {
        SplashOnboardingFlow(onFinished = { showSplashFlow = false })
        return
    }

    val languages = listOf(
        "English", "Spanish", "French", "German", "Portuguese", 
        "Arabic", "Hindi", "Chinese (Simplified)", "Japanese", "Russian"
    )

    val currencies = listOf(
        "USD" to "$ (US Dollar)",
        "BDT" to "৳ (Bangladeshi Taka)",
        "EUR" to "€ (Euro)",
        "GBP" to "£ (British Pound)",
        "INR" to "₹ (Indian Rupee)",
        "JPY" to "¥ (Japanese Yen)",
        "CAD" to "$ (Canadian Dollar)",
        "AED" to "د.إ (UAE Dirham)",
        "SAR" to "ر.س (Saudi Riyal)",
        "AUD" to "$ (Australian Dollar)"
    )

    CompositionLocalProvider(LocalAppLanguage provides selectedLanguage) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = BrandBackground
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BrandBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // STEP PROGRESS HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..4) {
                    val color = if (i <= step) BrandPurple else Color(0xFFE0E3EF)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
            }

            // CENTRAL BODY CONFIGURATION
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeIn())
                                .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeOut())
                        } else {
                            (slideInHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it } + fadeIn())
                                .togetherWith(slideOutHorizontally(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { it } + fadeOut())
                        }
                    },
                    label = "OnboardingStepTransition"
                ) { currentStep ->
                    when (currentStep) {
                        1 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BrandPurple,
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Your Data, Your Privacy",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandDarkIndigo,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.5).sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "This application is built 100% offline-first. Your financial credentials, balances, and attachments never leave your device. We store zero tracking logs and require no cloud login.",
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = BrandPurple,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "Encryption Built-in",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = BrandDarkIndigo
                                        )
                                        Text(
                                            text = "All spaces are locked securely inside Room SQLite sandbox storage.",
                                            fontSize = 13.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Personalize Setup",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandDarkIndigo,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.5).sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Name (Optional)") },
                                placeholder = { Text("e.g. Shahed") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_username_input"),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPurple) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    focusedLabelColor = BrandPurple,
                                    cursorColor = BrandPurple
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Select App Language".translated(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth(),
                                color = BrandDarkIndigo,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Scrollable list of languages
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    languages.forEach { lang ->
                                        val isSelected = selectedLanguage == lang
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) BrandPurple.copy(alpha = 0.08f) else Color.Transparent)
                                                .clickable { selectedLanguage = lang }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedLanguage = lang },
                                                colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = lang, 
                                                fontSize = 15.sp, 
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) BrandPurple else BrandDarkIndigo
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Sovereign Currency",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandDarkIndigo,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.5).sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Set reference default currency and cash float.",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            OutlinedTextField(
                                value = initialBalanceStr,
                                onValueChange = { initialBalanceStr = it },
                                label = { Text("Initial Cash Balance Wallet Float") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BrandPurple) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandPurple,
                                    focusedLabelColor = BrandPurple,
                                    cursorColor = BrandPurple
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Default Currency Base",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                modifier = Modifier.fillMaxWidth(),
                                color = BrandDarkIndigo,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(rememberScrollState())
                                        .padding(8.dp)
                                ) {
                                    currencies.forEach { (code, desc) ->
                                        val isSelected = selectedCurrency == code
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) BrandPurple.copy(alpha = 0.08f) else Color.Transparent)
                                                .clickable { selectedCurrency = code }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedCurrency = code },
                                                colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "$code - $desc", 
                                                fontSize = 15.sp, 
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) BrandPurple else BrandDarkIndigo
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    4 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Lock Privacy Settings",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandDarkIndigo,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = (-0.5).sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Restrict unauthorized dashboard glances with biometric passcode locks.",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = BrandPurple)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Biometric Fingerprint Lock", fontWeight = FontWeight.SemiBold, color = BrandDarkIndigo)
                                }
                                Switch(
                                    checked = useBiometrics,
                                    onCheckedChange = { useBiometrics = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandPurple,
                                        uncheckedThumbColor = Color(0xFF9CA3AF),
                                        uncheckedTrackColor = Color(0xFFE5E7EB)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Dialpad, contentDescription = null, tint = BrandPurple)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text("Setup PIN Password Code", fontWeight = FontWeight.SemiBold, color = BrandDarkIndigo)
                                }
                                Switch(
                                    checked = setupPin,
                                    onCheckedChange = { setupPin = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = BrandPurple,
                                        uncheckedThumbColor = Color(0xFF9CA3AF),
                                        uncheckedTrackColor = Color(0xFFE5E7EB)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            AnimatedVisibility(visible = setupPin) {
                                OutlinedTextField(
                                    value = pinValue,
                                    onValueChange = { if (it.length <= 4) pinValue = it },
                                    label = { Text("Enter 4-Digit PIN") },
                                    placeholder = { Text("e.g. 1234") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandPurple,
                                        focusedLabelColor = BrandPurple,
                                        cursorColor = BrandPurple
                                    )
                                )
                            }
                        }
                    }
                }
                }
            }

            // FOOTER NAVIGATION ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(
                        onClick = { step-- },
                        colors = ButtonDefaults.textButtonColors(contentColor = BrandPurple)
                    ) {
                        Text("Back".translated(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            // Finish and seed
                            viewModel.completeOnboarding(
                                userNameInput = name,
                                baseCurrencyCode = selectedCurrency,
                                initialCashBalance = initialBalanceStr.toDoubleOrNull() ?: 0.0,
                                appLanguage = selectedLanguage,
                                useBiometrics = useBiometrics,
                                pinLock = if (setupPin && pinValue.isNotEmpty()) pinValue else null
                            )
                            onComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .width(if (step == 4) 160.dp else 115.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = BrandPurple.copy(0.42f),
                            spotColor = BrandPurple.copy(0.42f)
                        )
                        .testTag("submit_onboarding_button")
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
                            Text(text = if (step == 4) "Get Started".translated() else "Next".translated(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
}
