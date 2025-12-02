package com.zerotrust.bankingdemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotrust.bankingdemo.ZeroTrustViewModel
import com.zerotrust.bankingdemo.ui.theme.TrustHighColor
import com.zerotrust.bankingdemo.ui.theme.TrustLowColor
import com.zerotrust.bankingdemo.ui.theme.TrustMediumColor

@Composable
fun LoginScreen(
    viewModel: ZeroTrustViewModel,
    onLoginSuccess: () -> Unit
) {
    // State lifted to ViewModel
    val username = viewModel.loginUsername
    val password = viewModel.loginPassword
    
    var passwordVisible by remember { mutableStateOf(false) }
    var typingPattern by remember { mutableStateOf<String>("Start typing to see pattern analysis...") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var showStepUpDialog by remember { mutableStateOf(false) }
    
    val trustScore = viewModel.trustScore
    val isBaselineEstablished = viewModel.isBaselineEstablished
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Header
        Text(
            text = "🔐 Secure Login",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Login Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Username
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue: TextFieldValue ->
                        viewModel.loginUsername = newValue
                        viewModel.behavioralMonitor.onTextChanged(newValue)?.let { pattern ->
                            typingPattern = """
                                Typing Pattern Detected:
                                • Rhythm: ${pattern.rhythmType}
                                • Avg Interval: ${pattern.avgInterval}ms
                                • Variance: ${"%.2f".format(pattern.variance)}
                                • Keystrokes: ${pattern.keystrokeCount}
                            """.trimIndent()
                            
                            if (isBaselineEstablished) {
                                viewModel.recalculateTrustScore()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter username") }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Password
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { newValue: TextFieldValue ->
                        viewModel.loginPassword = newValue
                        viewModel.behavioralMonitor.onTextChanged(newValue)?.let { pattern ->
                            typingPattern = """
                                Typing Pattern Detected:
                                • Rhythm: ${pattern.rhythmType}
                                • Avg Interval: ${pattern.avgInterval}ms
                                • Variance: ${"%.2f".format(pattern.variance)}
                                • Keystrokes: ${pattern.keystrokeCount}
                            """.trimIndent()
                            
                            if (isBaselineEstablished) {
                                viewModel.recalculateTrustScore()
                            }
                        }
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Login Button
                Button(
                    onClick = {
                        if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
                            when (val result = viewModel.validateLogin()) {
                                is ZeroTrustViewModel.LoginResult.Success -> onLoginSuccess()
                                is ZeroTrustViewModel.LoginResult.StepUpRequired -> showStepUpDialog = true
                                is ZeroTrustViewModel.LoginResult.InvalidCredentials -> {
                                    loginError = "Invalid credentials. Attempts: ${result.attempts}"
                                }
                                is ZeroTrustViewModel.LoginResult.SqlInjectionDetected -> {
                                    loginError = "ACCESS DENIED: Security Threat Detected"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = username.text.isNotEmpty() && password.text.isNotEmpty()
                ) {
                    Text("Login", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Combined Behavioral Analysis
                LoginBehavioralAnalysis(typingPattern)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Attack Simulations
                LoginAttackControls(viewModel)
            }
        }
        
        if (showStepUpDialog) {
            AlertDialog(
                onDismissRequest = { showStepUpDialog = false },
                title = { Text("Additional Verification Required") },
                text = { Text("Due to recent suspicious activity (multiple failed attempts), we need to verify your identity with a second factor.") },
                confirmButton = {
                    Button(onClick = { 
                        showStepUpDialog = false
                        onLoginSuccess() // Allow login after "verification"
                    }) {
                        Text("Verify & Continue")
                    }
                },
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        

        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Trust Score Card
        if (trustScore != null && isBaselineEstablished) {
            TrustScoreCard(trustScore = trustScore)
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "🛡️ Trust Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Establishing baseline patterns...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Info Text
        Text(
            text = "💡 For demo: Use any username/password\nYour typing pattern is being analyzed in real-time",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun TrustScoreCard(trustScore: com.zerotrust.bankingdemo.TrustScoreManager.TrustScore) {
    val color = when {
        trustScore.score >= 80 -> TrustHighColor
        trustScore.score >= 50 -> TrustMediumColor
        else -> TrustLowColor
    }
    
    val progressBar = "█".repeat(trustScore.score / 10) + "░".repeat(10 - trustScore.score / 10)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🛡️ Trust Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Trust Score: [$progressBar] ${trustScore.score}/100",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = color
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Level: ${trustScore.level.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Factors:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            
            trustScore.factors.values.forEach { factor ->
                Text(
                    text = "• ${factor.name}: ${factor.score}/100 - ${factor.status} (${factor.confidence}% confidence)",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "⚡ Calculation time: ${trustScore.calculationTimeMs}ms",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LoginBehavioralAnalysis(typingPattern: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "🛡️ Live Behavioral Analysis",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Typing Graph (Simplified)
            Text("Keystroke Dynamics:", style = MaterialTheme.typography.labelSmall)
            Text(
                text = typingPattern,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Location:", style = MaterialTheme.typography.labelSmall)
                    Text("37.77, -122.41", style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
                Column {
                    Text("Device Motion:", style = MaterialTheme.typography.labelSmall)
                    Text("Stable (0.04g)", style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun LoginAttackControls(viewModel: ZeroTrustViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🧪 Attack Simulation",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.performBotTypingSimulation() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("🤖 Bot Typing", style = MaterialTheme.typography.labelSmall)
            }
            
            Button(
                onClick = { 
                    com.zerotrust.bankingdemo.AttackSimulator.simulateLongDistanceJump()
                    viewModel.recalculateTrustScore()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(4.dp)
            ) {
                Text("✈️ Loc. Jump", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        OutlinedButton(
            onClick = { 
                viewModel.resetDemo()
            },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(4.dp)
        ) {
            Text("🔄 Reset", style = MaterialTheme.typography.labelSmall)
        }
    }
}
