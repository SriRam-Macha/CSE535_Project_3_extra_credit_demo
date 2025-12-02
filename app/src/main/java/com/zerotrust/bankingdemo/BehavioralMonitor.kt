package com.zerotrust.bankingdemo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * BehavioralMonitor - Captures user behavioral biometrics for Compose
 */
class BehavioralMonitor(context: Context) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private var lastAcceleration: FloatArray? = null
    private var shakeCount = 0
    private var lastShakeTime: Long = 0
    

    


    // Motion stability score (0-100), higher is more stable
    var deviceStabilityScore: Int = 100
        private set
        
    init {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                
                val acceleration = sqrt((x*x + y*y + z*z).toDouble()).toFloat()
                val currentAcceleration = acceleration - SensorManager.GRAVITY_EARTH
                
                // Detect shaking/instability
                if (currentAcceleration > 2.0f) { // Threshold for movement
                    val now = System.currentTimeMillis()
                    if (now - lastShakeTime > 500) {
                        shakeCount++
                        lastShakeTime = now
                        
                        // Decrease stability score on movement
                        deviceStabilityScore = (deviceStabilityScore - 5).coerceAtLeast(0)
                    }
                } else {
                    // Slowly recover stability if still
                    if (deviceStabilityScore < 100) {
                        deviceStabilityScore++
                    }
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
    
    
    private val keystrokeTimings = mutableListOf<Long>()
    
    // Expose for visualization
    fun getKeystrokeTimings(): List<Long> = keystrokeTimings.toList()
    
    private var lastKeyPressTime: Long = 0
    private var textLength: Int = 0
    
    /**
     * Monitor text field changes for typing patterns
     */
    fun onTextChanged(newValue: TextFieldValue): TypingPatternData? {
        val currentTime = System.currentTimeMillis()
        val newLength = newValue.text.length
        
        // Check if text was added (not deleted)
        if (newLength > textLength) {
            if (lastKeyPressTime != 0L) {
                val interval = currentTime - lastKeyPressTime
                
                // Only record intervals that represent actual typing (not field switches or long pauses)
                // Ignore intervals > 1500ms as they likely indicate field switching or pausing
                if (interval <= 1500) {
                    keystrokeTimings.add(interval)
                } else {
                    // Long pause detected - likely switched fields or paused typing
                    // Don't add this interval to avoid skewing the variance
                }
                
                // Return pattern on every keystroke if we have enough data (>= 3)
                if (keystrokeTimings.size >= 3) {
                    textLength = newLength
                    lastKeyPressTime = currentTime
                    return getCurrentTypingPattern()
                }
            }
            
            lastKeyPressTime = currentTime
        }
        
        textLength = newLength
        return null
    }

    // Generative AI Used: Claude AI (Antropic)
    // Purpose: Needed get the typing pattern recognition and calculation working
    // Prompt: "can you fix the typing pattern recognition and calculation"
    
    /**
     * Get current typing pattern statistics
     */
    fun getCurrentTypingPattern(): TypingPatternData {
        if (keystrokeTimings.isEmpty()) {
            return TypingPatternData(0L, 0.0, emptyList(), "No data", 0)
        }
        
        val avgInterval = keystrokeTimings.average().toLong()
        val variance = calculateVariance(keystrokeTimings.map { it.toDouble() })
        
        val rhythm = when {
            avgInterval < 150 -> "Fast"
            avgInterval < 300 -> "Normal"
            else -> "Slow"
        }
        
        return TypingPatternData(
            avgInterval = avgInterval,
            variance = variance,
            pressures = emptyList(), // Touch pressures not available in Compose
            rhythmType = rhythm,
            keystrokeCount = keystrokeTimings.size
        )
    }

    
    /**
     * Calculate statistical variance
     */
    private fun calculateVariance(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        
        val mean = values.average()
        val squaredDiffs = values.map { (it - mean).pow(2) }
        return squaredDiffs.average()
    }
    
    /**
     * Reset all captured patterns
     */
    fun reset() {
        keystrokeTimings.clear()
        lastKeyPressTime = 0
        textLength = 0
    }
    
    /**
     * Convert to TrustScoreManager format
     */
    fun convertToTypingPattern(): TrustScoreManager.TypingPattern {
        val pattern = getCurrentTypingPattern()
        return TrustScoreManager.TypingPattern(
            avgKeyInterval = pattern.avgInterval,
            variance = pattern.variance,
            pressureLevels = pattern.pressures
        )
    }

    
    data class TypingPatternData(
        val avgInterval: Long,
        val variance: Double,
        val pressures: List<Float>,
        val rhythmType: String,
        val keystrokeCount: Int
    )
}
