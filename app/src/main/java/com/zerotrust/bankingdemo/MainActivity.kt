package com.zerotrust.bankingdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zerotrust.bankingdemo.ui.screens.*
import com.zerotrust.bankingdemo.ui.theme.ZeroTrustBankingDemoTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ZeroTrustViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroTrustBankingDemoTheme {
                ZeroTrustApp(viewModel)
            }
        }
    }
}

@Composable
fun ZeroTrustApp(viewModel: ZeroTrustViewModel) {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "welcome"
        ) {
            composable("welcome") {
                WelcomeScreen(
                    onLoginClick = { navController.navigate("login") }
                )
            }
            
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navController.navigate("dashboard") }
                )
            }
            
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onTransferClick = { navController.navigate("transfer") }
                )
            }
            
            composable("transfer") {
                TransferScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            

        }
    }
}
