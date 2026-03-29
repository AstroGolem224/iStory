package com.storybuilder.core.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Haptic feedback utility for consistent vibration patterns
 */
class HapticFeedback(private val context: Context) {
    
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var enabled = true

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    /**
     * Light tick feedback for subtle interactions
     */
    fun performTick() {
        if (!enabled) return
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(10)
            }
        }
    }

    /**
     * Click feedback for button presses
     */
    fun performClick() {
        if (!enabled) return
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(20)
            }
        }
    }

    /**
     * Heavy click feedback for important actions
     */
    fun performHeavyClick() {
        if (!enabled) return
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(30)
            }
        }
    }

    /**
     * Double click feedback
     */
    fun performDoubleClick() {
        if (!enabled) return
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 30, 100, 30), -1)
            }
        }
    }

    /**
     * Success feedback pattern
     */
    fun performSuccess() {
        if (!enabled) return
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 50),
                    intArrayOf(0, 128, 0, 128),
                    -1
                )
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 50, 50, 50), -1)
            }
        }
    }

    /**
     * Error feedback pattern
     */
    fun performError() {
        if (!enabled) return
        
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 100, 100),
                    intArrayOf(0, 255, 0, 255),
                    -1
                )
                it.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 100, 100, 100), -1)
            }
        }
    }

    /**
     * Vibration for option selection
     */
    fun performOptionSelection() {
        performClick()
    }

    /**
     * Vibration for send button
     */
    fun performSend() {
        performHeavyClick()
    }

    /**
     * Vibration for mode switch
     */
    fun performModeSwitch() {
        performTick()
    }

    /**
     * Vibration for swipe action
     */
    fun performSwipe() {
        performTick()
    }

    companion object {
        @Volatile
        private var instance: HapticFeedback? = null

        fun getInstance(context: Context): HapticFeedback {
            return instance ?: synchronized(this) {
                instance ?: HapticFeedback(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

/**
 * Remember haptic feedback instance for Composables
 */
@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember { HapticFeedback.getInstance(context) }
}
