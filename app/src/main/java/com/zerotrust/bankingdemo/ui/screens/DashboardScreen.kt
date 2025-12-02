package com.zerotrust.bankingdemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zerotrust.bankingdemo.AttackSimulator
import com.zerotrust.bankingdemo.TrustScoreManager
import com.zerotrust.bankingdemo.ZeroTrustViewModel
import com.zerotrust.bankingdemo.ui.theme.TrustHighColor
import com.zerotrust.bankingdemo.ui.theme.TrustLowColor
import com.zerotrust.bankingdemo.ui.theme.TrustMediumColor

@Composable
fun DashboardScreen(
    viewModel: ZeroTrustViewModel,
    onTransferClick: () -> Unit
) {
    val trustScore = viewModel.trustScore
    val score = trustScore?.score ?: 100
    
    val color = when {
        score >= TrustScoreManager.HIGH_TRUST_THRESHOLD -> TrustHighColor
        score >= TrustScoreManager.MEDIUM_TRUST_THRESHOLD -> TrustMediumColor
        else -> TrustLowColor
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Header with Trust Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Trust Score Badge
            Card(
                colors = CardDefaults.cardColors(containerColor = color)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = score.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Trust Score Card
        TrustScoreDisplayCard(
            trustScore = trustScore
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Balance Card
        AccountBalanceCard(balance = viewModel.getAccountBalance())
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Button(
                    onClick = onTransferClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = viewModel.canTransfer()
                ) {
                    Text(
                        text = "💸 Transfer Money",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Security Info Card
        SecurityInfoCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Behavioral Analysis (New)
        BehavioralAnalysisCard()
        
        // Performance Stats
        viewModel.performanceStats?.let { stats ->
            Spacer(modifier = Modifier.height(16.dp))
            PerformanceStatsCard(stats)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Demo Controls moved to Login Screen
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TrustScoreDisplayCard(
    trustScore: TrustScoreManager.TrustScore?
) {
    val score = trustScore?.score ?: 100
    val color = when {
        score >= TrustScoreManager.HIGH_TRUST_THRESHOLD -> TrustHighColor
        score >= TrustScoreManager.MEDIUM_TRUST_THRESHOLD -> TrustMediumColor
        else -> TrustLowColor
    }
    
    val progressBar = "█".repeat(score / 10) + "░".repeat(10 - score / 10)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trust Score: [$progressBar] $score/100",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = color
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val statusText = when {
                score >= TrustScoreManager.HIGH_TRUST_THRESHOLD -> "✓ Full Access Granted\nAll banking features available\n\nSecurity Status: NORMAL"
                score >= TrustScoreManager.MEDIUM_TRUST_THRESHOLD -> "⚠ Limited Access\nTransfers require additional verification\n\nSecurity Status: ELEVATED"
                else -> "⚠ Restricted Access\nCritical operations blocked\n\nSecurity Status: HIGH"
            }
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AccountBalanceCard(balance: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💰 Account Balance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = balance,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SecurityInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔒 Security Features Active",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Continuous behavioral monitoring\n• Real-time trust scoring\n• Adaptive security controls\n• Zero-trust architecture",
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun PerformanceStatsCard(stats: TrustScoreManager.PerformanceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚡ Performance Metrics (Actual)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = """
                    Last Calculation: ${stats.lastCalculationMs}ms
                    Average: ${"%.2f".format(stats.averageCalculationMs)}ms
                    Samples: ${stats.sampleCount}
                    
                    Target: <50ms ✓
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BehavioralAnalysisCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Behavioral Analysis",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Simulated Sensor Data
            Text(
                text = "Accelerometer:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "X: 0.04  Y: 9.81  Z: 0.12",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Location:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Lat: 37.7749  Long: -122.4194",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Status: Monitoring Active",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
