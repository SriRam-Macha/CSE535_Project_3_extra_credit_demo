package com.zerotrust.bankingdemo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AttackSimulator - Manages simulation states for the demo
 * Only includes active simulations used in the UI
 */
object AttackSimulator {

    private val _isSuperHumanTypingSimulated = MutableStateFlow(false)
    val isSuperHumanTypingSimulated: StateFlow<Boolean> = _isSuperHumanTypingSimulated.asStateFlow()

    private val _isLongDistanceJumpSimulated = MutableStateFlow(false)
    val isLongDistanceJumpSimulated: StateFlow<Boolean> = _isLongDistanceJumpSimulated.asStateFlow()

    fun simulateSuperHumanTyping() {
        _isSuperHumanTypingSimulated.value = true
    }

    fun simulateLongDistanceJump() {
        _isLongDistanceJumpSimulated.value = true
    }
    
    fun resetSimulation() {
        _isSuperHumanTypingSimulated.value = false
        _isLongDistanceJumpSimulated.value = false
    }
}
