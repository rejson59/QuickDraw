package com.example.data

import android.content.Context
import android.content.SharedPreferences

object AppSettings {
    private const val PREFS_NAME = "quickdraw_settings"
    private const val KEY_STROKE_DELAY_MS = "key_stroke_delay_ms"
    private const val KEY_AUTO_HIDE_OVERLAY = "key_auto_hide_overlay"
    private const val KEY_HAPTIC_FEEDBACK = "key_haptic_feedback"
    private const val KEY_HAS_SEEN_TUTORIAL = "key_has_seen_tutorial"

    // Floating Bubble Settings
    private const val KEY_BUBBLE_EDGE = "key_bubble_edge"
    private const val KEY_BUBBLE_SHAPE = "key_bubble_shape"
    private const val KEY_BUBBLE_SIZE = "key_bubble_size"
    private const val KEY_BUBBLE_COLOR = "key_bubble_color"
    private const val KEY_BUBBLE_OPACITY = "key_bubble_opacity"
    private const val KEY_BUBBLE_INDICATOR = "key_bubble_indicator"
    private const val KEY_BUBBLE_HAPTIC = "key_bubble_haptic"

    const val DEFAULT_STROKE_DELAY_MS = 850L
    const val DEFAULT_BUBBLE_COLOR = 0xFF00E5FF.toInt() // Neon Cyan
    const val DEFAULT_BUBBLE_OPACITY = 0.85f

    enum class BubbleEdge {
        RIGHT, LEFT
    }

    enum class BubbleShape {
        PILL, CIRCLE, TAB
    }

    enum class BubbleSize {
        SMALL, MEDIUM, LARGE
    }

    enum class BubbleIndicator {
        DOT, BOLT, DRAW, NONE
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun hasSeenTutorial(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAS_SEEN_TUTORIAL, false)
    }

    fun setHasSeenTutorial(context: Context, seen: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAS_SEEN_TUTORIAL, seen).apply()
    }

    fun getStrokeDelayMs(context: Context): Long {
        return getPrefs(context).getLong(KEY_STROKE_DELAY_MS, DEFAULT_STROKE_DELAY_MS)
    }

    fun setStrokeDelayMs(context: Context, delayMs: Long) {
        getPrefs(context).edit().putLong(KEY_STROKE_DELAY_MS, delayMs).apply()
    }

    fun isHapticFeedbackEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAPTIC_FEEDBACK, true)
    }

    fun setHapticFeedbackEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply()
    }

    // --- Bubble Customization Getters & Setters ---

    fun getBubbleEdge(context: Context): BubbleEdge {
        val name = getPrefs(context).getString(KEY_BUBBLE_EDGE, BubbleEdge.RIGHT.name)
        return try {
            BubbleEdge.valueOf(name ?: BubbleEdge.RIGHT.name)
        } catch (e: Exception) {
            BubbleEdge.RIGHT
        }
    }

    fun setBubbleEdge(context: Context, edge: BubbleEdge) {
        getPrefs(context).edit().putString(KEY_BUBBLE_EDGE, edge.name).apply()
    }

    fun getBubbleShape(context: Context): BubbleShape {
        val name = getPrefs(context).getString(KEY_BUBBLE_SHAPE, BubbleShape.PILL.name)
        return try {
            BubbleShape.valueOf(name ?: BubbleShape.PILL.name)
        } catch (e: Exception) {
            BubbleShape.PILL
        }
    }

    fun setBubbleShape(context: Context, shape: BubbleShape) {
        getPrefs(context).edit().putString(KEY_BUBBLE_SHAPE, shape.name).apply()
    }

    fun getBubbleSize(context: Context): BubbleSize {
        val name = getPrefs(context).getString(KEY_BUBBLE_SIZE, BubbleSize.MEDIUM.name)
        return try {
            BubbleSize.valueOf(name ?: BubbleSize.MEDIUM.name)
        } catch (e: Exception) {
            BubbleSize.MEDIUM
        }
    }

    fun setBubbleSize(context: Context, size: BubbleSize) {
        getPrefs(context).edit().putString(KEY_BUBBLE_SIZE, size.name).apply()
    }

    fun getBubbleColor(context: Context): Int {
        return getPrefs(context).getInt(KEY_BUBBLE_COLOR, DEFAULT_BUBBLE_COLOR)
    }

    fun setBubbleColor(context: Context, color: Int) {
        getPrefs(context).edit().putInt(KEY_BUBBLE_COLOR, color).apply()
    }

    fun getBubbleOpacity(context: Context): Float {
        return getPrefs(context).getFloat(KEY_BUBBLE_OPACITY, DEFAULT_BUBBLE_OPACITY)
    }

    fun setBubbleOpacity(context: Context, opacity: Float) {
        getPrefs(context).edit().putFloat(KEY_BUBBLE_OPACITY, opacity.coerceIn(0.2f, 1f)).apply()
    }

    fun getBubbleIndicator(context: Context): BubbleIndicator {
        val name = getPrefs(context).getString(KEY_BUBBLE_INDICATOR, BubbleIndicator.DOT.name)
        return try {
            BubbleIndicator.valueOf(name ?: BubbleIndicator.DOT.name)
        } catch (e: Exception) {
            BubbleIndicator.DOT
        }
    }

    fun setBubbleIndicator(context: Context, indicator: BubbleIndicator) {
        getPrefs(context).edit().putString(KEY_BUBBLE_INDICATOR, indicator.name).apply()
    }

    fun isBubbleHapticEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BUBBLE_HAPTIC, true)
    }

    fun setBubbleHapticEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BUBBLE_HAPTIC, enabled).apply()
    }
}
