package com.zerotrust.bankingdemo

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ZeroTrustViewModel(application: Application) : AndroidViewModel(application) {
    
    private val trustScoreManager = TrustScoreManager(application)
    val behavioralMonitor = BehavioralMonitor(application)
    
    var trustScore by mutableStateOf<TrustScoreManager.TrustScore?>(null)
        private set
    
    var isBaselineEstablished by mutableStateOf(false)
        private set
    
    var performanceStats by mutableStateOf<TrustScoreManager.PerformanceStats?>(null)
        private set

    var loginAttempts by mutableStateOf(0)
        private set
        
    var isSqlInjectionDetected by mutableStateOf(false)
        private set

    // State for Login Screen fields (lifted to ViewModel for Bot Simulation)
    var loginUsername by mutableStateOf(TextFieldValue(""))
    var loginPassword by mutableStateOf(TextFieldValue(""))
    
    init {
        // Initialize with demo baseline after 2 seconds
        viewModelScope.launch {
            delay(2000)
            establishDemoBaseline()
        }
        
        // Start continuous monitoring
        startContinuousMonitoring()
    }
    
    private fun establishDemoBaseline() {
        val demoTypingPattern = TrustScoreManager.TypingPattern(
            avgKeyInterval = 200L,
            variance = 500.0,
            pressureLevels = listOf(0.5f, 0.6f, 0.5f, 0.7f, 0.6f)
        )
        
        trustScoreManager.setBaselineTypingPattern(demoTypingPattern)
        isBaselineEstablished = true
        
        // Calculate initial trust score
        recalculateTrustScore()
    }
    
    fun recalculateTrustScore() {
        val typingPattern = if (behavioralMonitor.getCurrentTypingPattern().keystrokeCount > 0) {
            behavioralMonitor.convertToTypingPattern()
        } else null
        
        trustScore = trustScoreManager.calculateTrustScore(
            currentTyping = typingPattern,
            deviceStability = behavioralMonitor.deviceStabilityScore,
            loginFailures = loginAttempts,
            sqlInjectionDetected = isSqlInjectionDetected
        )
        
        performanceStats = trustScoreManager.getPerformanceStats()
    }
    
    private fun startContinuousMonitoring() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // Update every 5 seconds
                if (isBaselineEstablished) {
                    recalculateTrustScore()
                }
            }
        }
    }
    

    
    fun getAccountBalance(): String {
        val score = trustScore?.score ?: 100
        return when {
            score >= TrustScoreManager.HIGH_TRUST_THRESHOLD -> "$25,847.32"
            score >= TrustScoreManager.MEDIUM_TRUST_THRESHOLD -> "$25,8**.** (Masked)"
            else -> "Hidden"
        }
    }
    
    fun canTransfer(): Boolean {
        val score = trustScore?.score ?: 100
        return score >= TrustScoreManager.MEDIUM_TRUST_THRESHOLD
    }
    
    fun getSecurityAction(): TrustScoreManager.SecurityAction {
        return trustScore?.let { trustScoreManager.getSecurityAction(it) } 
            ?: TrustScoreManager.SecurityAction.ALLOW
    }

    fun validateLogin(): LoginResult {
        val username = loginUsername.text
        val password = loginPassword.text

        // Check for SQL Injection patterns
        if (checkForSqlInjection(username) || checkForSqlInjection(password)) {
            isSqlInjectionDetected = true
            recalculateTrustScore()
            return LoginResult.SqlInjectionDetected
        }
        
        // Hardcoded credentials for demo
        if (username == "user" && password == "password") {
            if (loginAttempts >= 5) {
                // Even with correct password, require step-up if too many failures
                return LoginResult.StepUpRequired
            }
            loginAttempts = 0 // Reset on success
            recalculateTrustScore()
            return LoginResult.Success
        } else {
            loginAttempts++
            recalculateTrustScore()
            return LoginResult.InvalidCredentials(loginAttempts)
        }
    }
    
    fun performBotTypingSimulation() {
        viewModelScope.launch {
            // Clear fields
            loginUsername = TextFieldValue("")
            loginPassword = TextFieldValue("")
            delay(500)
            
            // Type "bot_user" rapidly
            val userTarget = "bot_user"
            for (i in 1..userTarget.length) {
                loginUsername = TextFieldValue(userTarget.substring(0, i))
                behavioralMonitor.onTextChanged(loginUsername)
                delay(20) // Super fast typing (20ms)
            }
            
            delay(200)
            
            // Type "password123" rapidly
            val passTarget = "password123"
            for (i in 1..passTarget.length) {
                loginPassword = TextFieldValue(passTarget.substring(0, i))
                behavioralMonitor.onTextChanged(loginPassword)
                delay(20)
            }
            
            // Trigger the attack simulation flag
            AttackSimulator.simulateSuperHumanTyping()
            recalculateTrustScore()
        }
    }

    sealed class LoginResult {
        object Success : LoginResult()
        data class InvalidCredentials(val attempts: Int) : LoginResult()
        object StepUpRequired : LoginResult()
        object SqlInjectionDetected : LoginResult()
    }
    
    fun checkForSqlInjection(input: String): Boolean {
        val patterns = listOf(
            "' OR '1'='1",
            "\" OR \"1\"=\"1",
            "; DROP TABLE",
            "--",
            "UNION SELECT"
        )
        return patterns.any { input.uppercase().contains(it) }
    }
    
    fun resetDemo() {
        // Reset internal state
        loginAttempts = 0
        isSqlInjectionDetected = false
        
        // Reset text fields
        loginUsername = TextFieldValue("")
        loginPassword = TextFieldValue("")
        
        // Reset attack simulation
        AttackSimulator.resetSimulation()
        
        // Reset behavioral monitor
        behavioralMonitor.reset()
        
        // Recalculate score (should return to 100/High)
        recalculateTrustScore()
    }
}
