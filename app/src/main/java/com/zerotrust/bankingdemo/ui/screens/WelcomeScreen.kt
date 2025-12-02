package com.zerotrust.bankingdemo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "Zero-Trust Banking Demo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Icon
        Text(
            text = "🔒",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Description
        Text(
            text = """
                This demo showcases zero-trust security principles:
                
                🔐 Continuous Verification
                Behavioral monitoring throughout your session
                
                📊 Risk-Based Security
                Dynamic trust scoring (0-100 scale)
                
                ✨ Invisible UX
                Seamless security without friction
                
                Features:
                • Typing pattern detection
                • Touch dynamics analysis
                • Real-time trust score
                • Adaptive security responses
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Login Button
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Login to Demo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        

        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Footer
        Text(
            text = "CSE 535 Mobile Computing\nProject 3: Zero-Trust Security Demo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
