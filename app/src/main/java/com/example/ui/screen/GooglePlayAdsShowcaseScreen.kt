package com.example.ui.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.translated
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePlayAdsShowcaseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val successMsgVal = "Success! Earned 1 Token reward!".translated()
    val toastMsgVal = "Reward token credited to your local profile!".translated()

    val consent by viewModel.personalizedAdsConsent.collectAsState()
    val tokens by viewModel.adShowcaseRewardTokens.collectAsState()
    val isAdFreeModeActive by viewModel.isAdFreeModeActive.collectAsState()

    // Screen State
    var showInterstitial by remember { mutableStateOf(false) }
    var activeAdCategory by remember { mutableStateOf("GOOD") } // GOOD or SOCIAL
    
    // Rewarded progress states
    var isWatchingRewarded by remember { mutableStateOf(false) }
    var rewardedProgress by remember { mutableStateOf(0f) }
    var rewardStatusText by remember { mutableStateOf("") }

    // Navigation and Info Expand states
    var explainPolicyExpanded by remember { mutableStateOf(false) }

    // Interstitial timer simulation
    var interstitialLoadProgress by remember { mutableStateOf(0f) }

    val purpleGradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF9333EA)))
    val greenGradient = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
    val orangeGradient = Brush.horizontalGradient(listOf(Color(0xFFFF6B3D), Color(0xFFEA580C)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .testTag("ads_showcase_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // HEADER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Color.White)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ads_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF111827)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Ads Compliance Hub".translated(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Google Play Policy sandbox".translated(),
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                // Tokens Reward Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF7C3AED).copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFF7C3AED).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Tokens",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$tokens Tokens".translated(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION: Policy Disclaimer Banner
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(0.04f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Play Verified Compliance".translated(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = "Transparent Ads Delivery Standard".translated(),
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "To publish on Google Play, apps must strictly differentiate advertisements from content, use secure certified SDKs, permit immediate dismissals, and request clear user data consent.".translated(),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF4B5563)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Info Detail
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .clickable { explainPolicyExpanded = !explainPolicyExpanded }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Read Detailed Play Store Ad Rules".translated(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                                Icon(
                                    imageVector = if (explainPolicyExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color(0xFF4B5563),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = explainPolicyExpanded) {
                            Column(
                                modifier = Modifier.padding(top = 10.dp, start = 4.dp, end = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InfoBulletRow("1. No Deceptive ads", "Ads must not pretend to be system alerts, buttons, or app interface elements.")
                                InfoBulletRow("2. Clear Close [X] Options", "Full-screen interstitial ads must have visible close buttons right away.")
                                InfoBulletRow("3. Family Protection", "Apps targeting children must only use AdMob certified family-safe creatives.")
                                InfoBulletRow("4. Clear sponsored badges", "Native integration feeds must explicitly label sponsored items proudly.")
                            }
                        }
                    }
                }

                // SECTION: Explicit Ads Consent Configuration
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(0.04f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Personalized Ad Consent".translated(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF111827)
                                    )
                                    Text(
                                        text = "Enable interest-matched context".translated(),
                                        fontSize = 11.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                            Switch(
                                checked = consent,
                                onCheckedChange = {
                                    viewModel.setPersonalizedAdsConsent(it)
                                    val msg = if (it) "Personalized ads enabled." else "Generalized context selected."
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF7C3AED))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "By checking this custom toggle, AdMob / Social Networks will tailor sponsorships to your saving categories, keeping privacy local. If disabled, non-personalized ads are shown contextually.".translated(),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                // SECTION: Creative Selection Toggles
                Text(
                    text = "AAd Sandbox Formats Showcase".translated(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF111827),
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )

                // Select Ad category chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeAdCategory == "GOOD",
                        onClick = { activeAdCategory = "GOOD" },
                        label = { Text("Budget & Utility Ads".translated()) },
                        leadingIcon = if (activeAdCategory == "GOOD") {
                            { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    FilterChip(
                        selected = activeAdCategory == "SOCIAL",
                        onClick = { activeAdCategory = "SOCIAL" },
                        label = { Text("Social Media Promos".translated()) },
                        leadingIcon = if (activeAdCategory == "SOCIAL") {
                            { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // AD FORMAT 1: NON-DISRUPTIVE SMART BANNER
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Smart Banner Ad".translated(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF374151)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("BANNER".translated(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actual Interactive Banner Ad Representation with active Suppression status
                        if (isAdFreeModeActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECFDF5))
                                    .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Ad-Free Token Suppression Active".translated(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF047857)
                                        )
                                        Text(
                                            text = "This Smart Banner has been successfully deactivated for 24 hours.".translated(),
                                            fontSize = 10.sp,
                                            color = Color(0xFF065F46)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (activeAdCategory == "GOOD") Color(0xFFF0FDF4) else Color(0xFFEFF6FF)
                                    )
                                    .border(
                                        1.dp,
                                        if (activeAdCategory == "GOOD") Color(0xFF86EFAC) else Color(0xFF93C5FD),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        Toast
                                            .makeText(
                                                context,
                                                "Redirecting to secure educational portal verified by Play Store policies.",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (activeAdCategory == "GOOD") Color(0xFF22C55E) else Color(0xFF3B82F6)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (activeAdCategory == "GOOD") Icons.Default.Savings else Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (activeAdCategory == "GOOD") Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFF3B82F6).copy(alpha = 0.15f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "Sponsored".translated(),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = if (activeAdCategory == "GOOD") Color(0xFF15803D) else Color(0xFF1D4ED8)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (activeAdCategory == "GOOD") "SafeSaver Plus".translated() else "AdLink Media Tech".translated(),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF111827)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (activeAdCategory == "GOOD") "Boost retirement funds by 3.5% APY easily.".translated() else "Reach millions of organic downloads contextually.".translated(),
                                                fontSize = 11.sp,
                                                color = Color(0xFF374151)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Sponsor link opened securely.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (activeAdCategory == "GOOD") Color(0xFF16A34A) else Color(0xFF2563EB)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Get".translated(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "✔ Play Store Compliance Check: Non-overlapping layout, clearly marked as 'Sponsored', matches standard padding margins without interfering with interactions.".translated(),
                            fontSize = 10.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // AD FORMAT 2: FEEDS SOCIAL NATIVE NATIVE SPONSOREDS MOCKS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. Social Media Native Feed Ad".translated(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF374151)
                              )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("NATIVE FEED".translated(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Post author
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3F4F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Sarah Jenkins (Financial Analyst)".translated(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF111827))
                                Text("Posted 2 hours ago • New York".translated(), fontSize = 10.sp, color = Color(0xFF6B7280))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "I just set up a weekly recurring budget in Workspace Isolation. Highly recommend keeping track of monthly outflows. Look at this secure partner app suggestion. #Fintech".translated(),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF374151)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // High fidelity native Ad component tucked into the social post!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9FAFB))
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                // Ad marker header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SPONSORED PROMOTION".translated(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFEA580C)
                                        )
                                    }

                                    // Clear close/hide button to comply with control rules
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hide ad spacing",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "Play Store Compliance: Users can opt-out of specific native recommendations.",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Featured image promo placeholder
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (activeAdCategory == "GOOD") Color(0xFFEEF2F6) else Color(0xFFFFF7ED)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (activeAdCategory == "GOOD") Icons.Outlined.AutoGraph else Icons.Outlined.ThumbUpAlt,
                                            contentDescription = null,
                                            tint = if (activeAdCategory == "GOOD") Color(0xFF3B82F6) else Color(0xFFF97316),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (activeAdCategory == "GOOD") "Automated Portfolio Optimizer" else "Trendy Gadgets & Gear Store",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF1F2937),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (activeAdCategory == "GOOD") "Utilize custom neural parameters to re-balance stocks with low risk." else "Premium quality accessories. Safe worldwide shipping. Order today and get 25% off.",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = Color(0xFF4B5563)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ads.financeledger.com",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Navigating safely.", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                                        contentPadding = PaddingValues(horizontal = 14.dp),
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Learn More".translated(), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✔ Play Store Compliance Check: Clearly labeled 'Sponsored' header. Dismissible hide button, distinct boundary color frame.".translated(),
                            fontSize = 10.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // AD FORMAT 3: INTERSTITIAL COMPLIANT TRIGGER
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. Interstitial (Full Screen) Ad".translated(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF374151)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("INTERSTITIAL".translated(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Play Store policies mandate that full screen interstitial ads cannot display instantly upon screen transitions, must feature a prominent, clear exit button immediately, and should be easily closeable. Trap designs are strictly forbidden.".translated(),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                interstitialLoadProgress = 0f
                                showInterstitial = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("interstitial_ad_trigger"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trigger Compliant Fullscreen Ad".translated(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // AD FORMAT 4: REWARDED PROGRESS CHANNELS
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "4. Rewarded Video Ad".translated(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF374151)
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("REWARDED VIDEO".translated(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Users must explicitly opt-in to watch rewarded videos. On completion, the app must credit the specified reward points securely.".translated(),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF6B7280)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isWatchingRewarded) {
                            // Watch progressive bar mock
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF9FAFB))
                                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            progress = { rewardedProgress },
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFFF59E0B),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Streaming Compliant Ad Promo Video...".translated(), fontSize = 11.sp, color = Color(0xFF1F2937))
                                    }
                                    Text(
                                        "${(fiveSecondsCountdown(rewardedProgress))}s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { rewardedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFFF59E0B),
                                    trackColor = Color(0xFFFEF3C7)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    isWatchingRewarded = true
                                    rewardedProgress = 0f
                                    rewardStatusText = ""
                                    scope.launch {
                                        for (i in 1..50) {
                                            delay(100)
                                            rewardedProgress = i / 50f
                                        }
                                        viewModel.earnRewardToken()
                                        rewardStatusText = successMsgVal
                                        isWatchingRewarded = false
                                        Toast.makeText(context, toastMsgVal, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("rewarded_ad_trigger"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.OndemandVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Watch Rewarded Ad (+1 Token)".translated(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (rewardStatusText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(rewardStatusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                }
                            }
                        }
                    }
                }
            }
        }

        // FULL SCREEN COMPLIANT INTERSTITIAL OVERLAY
        AnimatedVisibility(
            visible = showInterstitial,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable { /* Block actions underneath overlay */ }
            ) {
                // Main Dialog/Billboard style
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header with instant close option
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "SPONSORED HIGHLIGHT".translated(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C3AED)
                                )
                            }
                            
                            // Exit Close X button immediately available
                            IconButton(
                                onClick = { showInterstitial = false },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFF3F4F6), CircleShape)
                                    .testTag("interstitial_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Ad Spacing",
                                    tint = Color(0xFF1F2937),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Large beautiful graphic placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(purpleGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Fortress Ledger Backup".translated(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Play Store Certified Vault partner".translated(),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Never lose your financial ledgers. Secure 256-bit military-grade encryption back-ups configured natively in just seconds.".translated(),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF4B5563),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { showInterstitial = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Close Ad".translated(), color = Color(0xFF4B5563))
                            }

                            Button(
                                onClick = {
                                    showInterstitial = false
                                    Toast.makeText(context, "Sponsor backup service activated.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Try Fortress Free".translated(), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This interstitial ad is fully compliant with Google Play Store guidelines. You can close it instantly without any penalty or fake waits.".translated(),
                            fontSize = 10.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoBulletRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = Color(0xFF7C3AED), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF374151))
            Text(desc, fontSize = 10.sp, color = Color(0xFF6B7280))
        }
    }
}

private fun fiveSecondsCountdown(progress: Float): Int {
    val left = 5 - (progress * 5).toInt()
    return if (left < 0) 0 else left
}
