package com.zerotrust.bankingdemo

import android.content.Context
import android.location.Location
import android.os.Build
import kotlin.math.abs

/**
 * TrustScoreManager - Core component for Zero-Trust Security
 * Includes built-in performance measurement
 */
class TrustScoreManager(private val context: Context) {
    
    private var currentTrustScore: Int = 100
    private var baselineTypingPattern: TypingPattern? = null
    
    // Performance metrics
    var lastCalculationTimeMs: Long = 0
        private set
    var averageCalculationTimeMs: Double = 0.0
        private set
    private val calculationTimes = mutableListOf<Long>()
    
    companion object {
        const val HIGH_TRUST_THRESHOLD = 80
        const val MEDIUM_TRUST_THRESHOLD = 50
        const val LOW_TRUST_THRESHOLD = 30
        
        // Redistributed weights after removing touch pattern (was 25%)
        const val WEIGHT_TYPING = 0.40  // Increased from 30%
        const val WEIGHT_DEVICE = 0.25  // Increased from 20%
        const val WEIGHT_LOCATION = 0.20  // Increased from 15%
        const val WEIGHT_TIME = 0.15  // Increased from 10%
    }
    
    data class TrustScore(
        val score: Int,
        val level: TrustLevel,
        val factors: Map<String, FactorScore>,
        val calculationTimeMs: Long
    )
    
    data class FactorScore(
        val name: String,
        val score: Int,
        val confidence: Int,
        val status: String
    )
    
    enum class TrustLevel {
        HIGH, MEDIUM, LOW, CRITICAL
    }
    
    data class TypingPattern(
        val avgKeyInterval: Long,
        val variance: Double,
        val pressureLevels: List<Float>
    )
    
    fun setBaselineTypingPattern(pattern: TypingPattern) {
        baselineTypingPattern = pattern
    }
    
    /**
     * Calculate trust score with performance measurement
     */
    fun calculateTrustScore(
        currentTyping: TypingPattern? = null,
        location: Location? = null,
        deviceStability: Int = 100,
        loginFailures: Int = 0,
        sqlInjectionDetected: Boolean = false
    ): TrustScore {
        val startTime = System.nanoTime()
        
        val factors = mutableMapOf<String, FactorScore>()
        var weightedScore = 0.0
        
        // 1. Typing Pattern Analysis
        val typingScore = if (AttackSimulator.isSuperHumanTypingSimulated.value) {
            FactorScore("Typing", 10, 99, "🤖 Bot Detected")
        } else if (currentTyping != null && baselineTypingPattern != null) {
            analyzeTypingPattern(currentTyping, baselineTypingPattern!!)
        } else {
            FactorScore("Typing", 100, 0, "No baseline")
        }
        factors["typing"] = typingScore
        weightedScore += typingScore.score * WEIGHT_TYPING
        
        // 2. Device Integrity Check
        val deviceScore = checkDeviceIntegrity()
        factors["device"] = deviceScore
        weightedScore += deviceScore.score * WEIGHT_DEVICE
        
        // Motion Stability (Part of Device Score)
        if (deviceStability < 80) {
            weightedScore -= (100 - deviceStability) * 0.1
        }
        
        // 3. Location Analysis
        val locationScore = if (AttackSimulator.isLongDistanceJumpSimulated.value) {
            FactorScore("Location", 5, 99, "✈️ Impossible Travel")
        } else {
            analyzeLocation(location)
        }
        factors["location"] = locationScore
        weightedScore += locationScore.score * WEIGHT_LOCATION
        
        // 4. Time-based Analysis
        val timeScore = analyzeTimeContext()
        factors["time"] = timeScore
        weightedScore += timeScore.score * WEIGHT_TIME

        // Critical Security Events Override
        if (loginFailures >= 5) {
            weightedScore = weightedScore.coerceAtMost(40.0)
            factors["security"] = FactorScore("Security", 40, 100, "⚠ Too many attempts")
        }
        
        if (sqlInjectionDetected) {
            weightedScore = 0.0
            factors["security"] = FactorScore("Security", 0, 100, "☠️ SQL Injection")
        }
        
        currentTrustScore = weightedScore.toInt().coerceIn(0, 100)
        
        val level = when {
            currentTrustScore >= HIGH_TRUST_THRESHOLD -> TrustLevel.HIGH
            currentTrustScore >= MEDIUM_TRUST_THRESHOLD -> TrustLevel.MEDIUM
            currentTrustScore >= LOW_TRUST_THRESHOLD -> TrustLevel.LOW
            else -> TrustLevel.CRITICAL
        }
        
        // Calculate elapsed time
        val endTime = System.nanoTime()
        lastCalculationTimeMs = (endTime - startTime) / 1_000_000 // Convert to milliseconds
        
        // Update average
        calculationTimes.add(lastCalculationTimeMs)
        if (calculationTimes.size > 100) calculationTimes.removeAt(0)
        averageCalculationTimeMs = calculationTimes.average()
        
        return TrustScore(currentTrustScore, level, factors, lastCalculationTimeMs)
    }
    // Generative AI Used: Claude AI (Antropic)
    // Purpose: Needed to improve the calculation and pattern regognetion only
    // Prompt: "can you fix the pattern recognition and calculation"

    private fun analyzeTypingPattern(current: TypingPattern, baseline: TypingPattern): FactorScore {
        val intervalDiff = abs(current.avgKeyInterval - baseline.avgKeyInterval).toDouble()
        val maxAllowedDiff = baseline.avgKeyInterval * 5.0 // Extremely generous - allows 5x variation
        
        val intervalSimilarity = (1.0 - (intervalDiff / maxAllowedDiff).coerceIn(0.0, 1.0)) * 100
        
        val varianceDiff = abs(current.variance - baseline.variance)
        val maxVarianceDiff = baseline.variance * 10.0 // Allow 10x variance difference
        val varianceSimilarity = (1.0 - (varianceDiff / maxVarianceDiff).coerceIn(0.0, 1.0)) * 100
        
        val overallScore = ((intervalSimilarity + varianceSimilarity) / 2).toInt()
        val confidence = if (current.pressureLevels.size > 10) 90 else 60
        
        val status = when {
            overallScore >= 50 -> "✓ Match" // Very low threshold - only flag extreme anomalies
            overallScore >= 25 -> "⚠ Suspicious"
            else -> "✗ Anomaly"
        }
        
        return FactorScore("Typing", overallScore, confidence, status)
    }
    

    
    private fun checkDeviceIntegrity(): FactorScore {
        var score = 100
        val issues = mutableListOf<String>()
        
        if (isDeviceRooted()) {
            score -= 50
            issues.add("Rooted")
        }
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            score -= 20
            issues.add("Outdated OS")
        }
        
        if (isDeveloperModeEnabled()) {
            score -= 10
            issues.add("Dev mode")
        }
        
        val status = if (issues.isEmpty()) "✓ Secure" else "⚠ ${issues.joinToString(", ")}"
        
        return FactorScore("Device", score.coerceIn(0, 100), 95, status)
    }
    
    private fun analyzeLocation(location: Location?): FactorScore {
        if (location == null) {
            return FactorScore("Location", 70, 50, "Unknown")
        }
        
        val score = 85
        val confidence = 75
        
        return FactorScore("Location", score, confidence, "✓ Expected")
    }
    
    private fun analyzeTimeContext(): FactorScore {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        val score = when (currentHour) {
            in 6..22 -> 100
            in 23..23, in 0..1 -> 80
            else -> 60
        }
        
        val status = when (currentHour) {
            in 6..22 -> "✓ Normal hours"
            in 23..23, in 0..1 -> "⚠ Late night"
            else -> "⚠ Unusual time"
        }
        
        return FactorScore("Time", score, 90, status)
    }
    
    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        return paths.any { java.io.File(it).exists() }
    }
    
    private fun isDeveloperModeEnabled(): Boolean {
        return try {
            android.provider.Settings.Secure.getInt(
                context.contentResolver,
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }
    
    fun getSecurityAction(trustScore: TrustScore): SecurityAction {
        return when (trustScore.level) {
            TrustLevel.HIGH -> SecurityAction.ALLOW
            TrustLevel.MEDIUM -> SecurityAction.STEP_UP_AUTH
            TrustLevel.LOW -> SecurityAction.CHALLENGE
            TrustLevel.CRITICAL -> SecurityAction.DENY
        }
    }
    
    enum class SecurityAction {
        ALLOW,
        STEP_UP_AUTH,
        CHALLENGE,
        DENY
    }
    

    
    /**
     * Get performance statistics
     */
    fun getPerformanceStats(): PerformanceStats {
        return PerformanceStats(
            lastCalculationMs = lastCalculationTimeMs,
            averageCalculationMs = averageCalculationTimeMs,
            sampleCount = calculationTimes.size
        )
    }
    
    data class PerformanceStats(
        val lastCalculationMs: Long,
        val averageCalculationMs: Double,
        val sampleCount: Int
    )
}
