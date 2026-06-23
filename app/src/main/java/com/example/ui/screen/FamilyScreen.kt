package com.example.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Account
import com.example.data.model.FamilyMember
import com.example.data.model.SpaceType
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.viewmodel.ExpenseViewModel

@Composable
fun FamilyScreen(viewModel: ExpenseViewModel) {
    val context = LocalContext.current
    val activeSpace by viewModel.activeSpace.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val settlements by viewModel.settlements.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val baseSymbol = activeSpace?.currency ?: "$"

    var showCreateMemberDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }
    var showSettleUpDialog by remember { mutableStateOf(false) }

    // Forms
    var newMemberName by remember { mutableStateOf("") }
    var newMemberRole by remember { mutableStateOf("MEMBER") } // ADMIN, MEMBER, CONTRIBUTOR

    // Compute Netted Debts matrix
    val debts = viewModel.calculateDebtBalances()

    if (activeSpace?.type != SpaceType.FAMILY) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Select a Family Space to begin", fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Switch your active workspace from the top profile bar switcher dropdown to allocate expenses among family members.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MODULE TITLE HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Family Workspace Ledger", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { showJoinCodeDialog = true },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = "Invite QR", tint = MaterialTheme.colorScheme.primary)
                }
                Button(
                    onClick = { showCreateMemberDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp)
                }
            }
        }

        // SHARED FAMILY WALLET POOL OVERVIEW
        val poolAccount = accounts.find { it.id.startsWith("account_pool_") }
        if (poolAccount != null) {
            val poolBalance = viewModel.calculateAccountBalance(poolAccount)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Shared Wallet Pool Account", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(poolAccount.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Text("$baseSymbol${String.format("%,.2f", poolBalance)}", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        // DEBTS MATRIX SECTION (NETTING IOU OUTCOMES)
        Text("Optimal Netting settlement (IOU Ledger)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (debts.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ThumbsUpDown, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("All members settled up. No debts owed!", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    debts.forEach { debt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(debt.fromMemberName, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                                    Text(" owes ", fontSize = 12.sp)
                                    Text(debt.toMemberName, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("Debt netted optimal split suggestion", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = { showSettleUpDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Settle Up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                }
            }
        }

        // FAMILY MEMBERS MANAGEMENT
        Text("Family Space Members list", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (familyMembers.isEmpty()) {
                    Text("No registered members. Click Add to invite.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    familyMembers.forEach { mem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(mem.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(mem.role, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }

        // HISTORICAL IMMUTABLE SETTLEMENT HISTORY
        Text("Immutable Settle Ledger History", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (settlements.isEmpty()) {
                    Text("No logged settlement transfers in history log.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    settlements.forEach { settle ->
                        val fromName = familyMembers.find { it.id == settle.fromMemberId }?.name ?: "Unknown"
                        val toName = familyMembers.find { it.id == settle.toMemberId }?.name ?: "Unknown"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(fromName, fontWeight = FontWeight.SemiBold)
                                    Text(" repaid ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(toName, fontWeight = FontWeight.SemiBold)
                                }
                                Text("Note: ${settle.note}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("+$baseSymbol${settle.amount}", fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50), fontSize = 13.sp)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }

    if (showCreateMemberDialog) {
        AlertDialog(
            onDismissRequest = { showCreateMemberDialog = false },
            title = { Text("Invite Member Slot") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Family Member Name") },
                        placeholder = { Text("e.g. Rachel") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Privilege Role Permission:", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("ADMIN", "MEMBER", "CONTRIBUTOR").forEach { role ->
                            FilterChip(
                                selected = newMemberRole == role,
                                onClick = { newMemberRole = role },
                                label = { Text(role, fontSize = 10.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newMemberName.isNotEmpty()) {
                            viewModel.createFamilyMember(newMemberName, newMemberRole)
                            showCreateMemberDialog = false
                            newMemberName = ""
                        }
                    }
                ) {
                    Text("Add Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateMemberDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showJoinCodeDialog) {
        val invitePayload = "expanager-invite-space:${activeSpace?.id ?: "null"}-key"
        AlertDialog(
            onDismissRequest = { showJoinCodeDialog = false },
            title = { Text("Space Invite Token Pair", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(90.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Share Invite Code:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = invitePayload,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Other family device can copy-paste this direct invite token inside Join Space dialog to pair in active offline sync.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showJoinCodeDialog = false }) { Text("Close") }
            }
        )
    }

    if (showSettleUpDialog) {
        var debtorId by remember { mutableStateOf(familyMembers.firstOrNull()?.id ?: "") }
        var creditorId by remember { mutableStateOf(familyMembers.firstOrNull { it.id != debtorId }?.id ?: "") }
        var settleAmountStr by remember { mutableStateOf("") }
        var settleNoteStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSettleUpDialog = false },
            title = { Text("Log Cash Settlement") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Payer (Repaying):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Card(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Row(modifier = Modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            familyMembers.forEach { m ->
                                FilterChip(
                                    selected = debtorId == m.id,
                                    onClick = { debtorId = m.id },
                                    label = { Text(m.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    Text("Receiver (Owed To):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Card(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                        Row(modifier = Modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            familyMembers.filter { it.id != debtorId }.forEach { m ->
                                FilterChip(
                                    selected = creditorId == m.id,
                                    onClick = { creditorId = m.id },
                                    label = { Text(m.name, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = settleAmountStr,
                        onValueChange = { settleAmountStr = it },
                        label = { Text("Settlement Cash Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = settleNoteStr,
                        onValueChange = { settleNoteStr = it },
                        label = { Text("Settle notes details") },
                        placeholder = { Text("e.g. Paid Rachel cash back") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = settleAmountStr.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0 && debtorId.isNotEmpty() && creditorId.isNotEmpty()) {
                            viewModel.logFamilySettlement(debtorId, creditorId, parsed, settleNoteStr)
                            showSettleUpDialog = false
                            settleAmountStr = ""
                            settleNoteStr = ""
                            Toast.makeText(context, "Settlement audit record successfully inserted!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Register Settlement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleUpDialog = false }) { Text("Cancel") }
            }
        )
    }
}
