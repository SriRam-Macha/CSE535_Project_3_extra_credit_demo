package com.zerotrust.bankingdemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotrust.bankingdemo.TrustScoreManager
import com.zerotrust.bankingdemo.ZeroTrustViewModel

import com.zerotrust.bankingdemo.ui.theme.TrustHighColor
import com.zerotrust.bankingdemo.ui.theme.TrustLowColor
import com.zerotrust.bankingdemo.ui.theme.TrustMediumColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: ZeroTrustViewModel,
    onBack: () -> Unit
) {
    var recipient by remember { mutableStateOf(TextFieldValue("")) }
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogContent by remember { mutableStateOf<DialogContent?>(null) }
    
    
    val trustScore = viewModel.trustScore
    val score = trustScore?.score ?: 100
    

    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Trust Score Display
            trustScore?.let {
                TrustScoreCard(trustScore = it)
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // Transfer Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Transfer Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recipient
                    Text(
                        text = "Recipient",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { 
                            recipient = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Account number or email") }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Amount
                    Text(
                        text = "Amount ($)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Typing Graph

                    
                    // Transfer Button
                    Button(
                        onClick = {
                            if (recipient.text.isNotEmpty() && amount.text.isNotEmpty()) {
                                val action = viewModel.getSecurityAction()
                                dialogContent = when (action) {
                                    TrustScoreManager.SecurityAction.ALLOW -> DialogContent.Success(recipient.text, amount.text, score)
                                    TrustScoreManager.SecurityAction.STEP_UP_AUTH -> DialogContent.StepUp(recipient.text, amount.text, score)
                                    TrustScoreManager.SecurityAction.CHALLENGE -> DialogContent.Challenge(recipient.text, amount.text, score)
                                    TrustScoreManager.SecurityAction.DENY -> DialogContent.Deny(score)
                                }
                                showDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = recipient.text.isNotEmpty() && amount.text.isNotEmpty()
                    ) {
                        Text(
                            text = "Transfer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            

            
            Spacer(modifier = Modifier.height(16.dp))
            

        }
    }
    
    // Show dialog based on security action
    if (showDialog && dialogContent != null) {
        TransferDialog(
            content = dialogContent!!,
            onDismiss = { 
                showDialog = false
                dialogContent = null
            },
            onSuccess = {
                showDialog = false
                dialogContent = null
                onBack()
            }
        )
    }
}

sealed class DialogContent {
    data class Success(val recipient: String, val amount: String, val score: Int) : DialogContent()
    data class StepUp(val recipient: String, val amount: String, val score: Int) : DialogContent()
    data class Challenge(val recipient: String, val amount: String, val score: Int) : DialogContent()
    data class Deny(val score: Int) : DialogContent()
}

@Composable
fun TransferDialog(
    content: DialogContent,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    when (content) {
        is DialogContent.Success -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("✓ Transfer Approved") },
                text = {
                    Text("""
                        Transfer of $${content.amount} to ${content.recipient} has been approved.
                        
                        Trust Score: ${content.score}/100
                        Security Level: NORMAL
                        
                        No additional verification required.
                    """.trimIndent())
                },
                confirmButton = {
                    Button(onClick = onSuccess) {
                        Text("OK")
                    }
                }
            )
        }
        is DialogContent.StepUp -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("⚠ Additional Verification Required") },
                text = {
                    Text("""
                        Transfer of $${content.amount} to ${content.recipient} requires additional verification.
                        
                        Trust Score: ${content.score}/100
                        Security Level: ELEVATED
                        
                        In a production app, this would trigger:
                        • SMS/Email OTP
                        • Biometric authentication
                        • Security question
                        
                        For this demo, we'll approve the transfer.
                    """.trimIndent())
                },
                confirmButton = {
                    Button(onClick = onSuccess) {
                        Text("Verify & Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }
        is DialogContent.Challenge -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("⚠ Strong Authentication Required") },
                text = {
                    Text("""
                        Suspicious behavior detected. Strong authentication required.
                        
                        Trust Score: ${content.score}/100
                        Security Level: HIGH
                        
                        In a production app, this would trigger:
                        • Multi-factor authentication
                        • CAPTCHA challenge
                        • Security questions
                        • Biometric verification
                        
                        Transfer may be delayed for review.
                    """.trimIndent())
                },
                confirmButton = {
                    Button(onClick = onSuccess) {
                        Text("Complete Challenge")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }
        is DialogContent.Deny -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("✗ Transfer Blocked") },
                text = {
                    Text("""
                        This transfer has been blocked for your security.
                        
                        Trust Score: ${content.score}/100
                        Security Level: CRITICAL
                        
                        Reasons:
                        • Suspicious behavioral patterns detected
                        • Unusual access patterns
                        • Device integrity concerns
                        
                        Please contact customer support or try again later.
                    """.trimIndent())
                },
                confirmButton = {
                    Button(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
