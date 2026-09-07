package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.QuickDrawApplication
import com.example.data.AppSettings
import com.example.engine.GestureRecognizer
import com.example.model.GestureEntity
import com.example.model.GesturePoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.abs

class QuickDrawOverlayService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var floatingTriggerView: View? = null
    private var drawingOverlayView: View? = null
    private var triggerParams: WindowManager.LayoutParams? = null

    private var cachedGestures: List<GestureEntity> = emptyList()
    private var isOverlayVisible = false

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        startForegroundNotification()
        observeGestures()
        setupFloatingTrigger()
    }

    private fun startForegroundNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, QuickDrawOverlayService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, QuickDrawApplication.CHANNEL_ID)
            .setContentTitle("QuickDraw Aktywator")
            .setContentText("Dotknij krawędzi ekranu, aby narysować gest")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Zatrzymaj", stopPendingIntent)
            .build()

        startForeground(QuickDrawApplication.NOTIFICATION_ID, notification)
    }

    private fun observeGestures() {
        serviceScope.launch {
            QuickDrawApplication.instance.repository.enabledGestures.collectLatest { gestures ->
                cachedGestures = gestures
            }
        }
    }

    private fun setupFloatingTrigger(savedY: Int = 0) {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // Read customized settings
        val edge = AppSettings.getBubbleEdge(this)
        val bubbleShapeSetting = AppSettings.getBubbleShape(this)
        val size = AppSettings.getBubbleSize(this)
        val baseColor = AppSettings.getBubbleColor(this)
        val opacity = AppSettings.getBubbleOpacity(this)
        val indicator = AppSettings.getBubbleIndicator(this)

        val isLeftEdge = edge == AppSettings.BubbleEdge.LEFT

        // Calculate dimensions
        val (widthDp, heightDp) = when (bubbleShapeSetting) {
            AppSettings.BubbleShape.CIRCLE -> when (size) {
                AppSettings.BubbleSize.SMALL -> 40 to 40
                AppSettings.BubbleSize.MEDIUM -> 50 to 50
                AppSettings.BubbleSize.LARGE -> 62 to 62
            }
            AppSettings.BubbleShape.TAB -> when (size) {
                AppSettings.BubbleSize.SMALL -> 18 to 64
                AppSettings.BubbleSize.MEDIUM -> 24 to 86
                AppSettings.BubbleSize.LARGE -> 30 to 110
            }
            AppSettings.BubbleShape.PILL -> when (size) {
                AppSettings.BubbleSize.SMALL -> 18 to 60
                AppSettings.BubbleSize.MEDIUM -> 24 to 82
                AppSettings.BubbleSize.LARGE -> 32 to 108
            }
        }

        val widthPx = dpToPx(widthDp)
        val heightPx = dpToPx(heightDp)

        val params = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = (if (isLeftEdge) Gravity.START else Gravity.END) or Gravity.CENTER_VERTICAL
            x = 0
            y = savedY
        }
        triggerParams = params

        // Compute color with alpha
        val alphaInt = (opacity * 255).toInt().coerceIn(40, 255)
        val bubbleColorWithAlpha = (baseColor and 0x00FFFFFF) or (alphaInt shl 24)

        val trigger = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                val r = dpToPx(when (bubbleShapeSetting) {
                    AppSettings.BubbleShape.CIRCLE -> (widthDp / 2)
                    AppSettings.BubbleShape.TAB -> 16
                    AppSettings.BubbleShape.PILL -> 14
                }).toFloat()

                when (bubbleShapeSetting) {
                    AppSettings.BubbleShape.CIRCLE -> {
                        setShape(GradientDrawable.OVAL)
                    }
                    AppSettings.BubbleShape.TAB -> {
                        setShape(GradientDrawable.RECTANGLE)
                        if (isLeftEdge) {
                            // Rounded on the right side entering screen, flat against left edge
                            cornerRadii = floatArrayOf(0f, 0f, r, r, r, r, 0f, 0f)
                        } else {
                            // Rounded on the left side entering screen, flat against right edge
                            cornerRadii = floatArrayOf(r, r, 0f, 0f, 0f, 0f, r, r)
                        }
                    }
                    AppSettings.BubbleShape.PILL -> {
                        setShape(GradientDrawable.RECTANGLE)
                        cornerRadius = r
                    }
                }

                setColor(bubbleColorWithAlpha)
                setStroke(dpToPx(1), 0xDDFFFFFF.toInt())
            }

            // Indicator inside bubble
            when (indicator) {
                AppSettings.BubbleIndicator.DOT -> {
                    val dot = View(this@QuickDrawOverlayService).apply {
                        background = GradientDrawable().apply {
                            setShape(GradientDrawable.OVAL)
                            setColor(0xFFFFFFFF.toInt())
                        }
                        val dotSize = dpToPx(if (size == AppSettings.BubbleSize.SMALL) 5 else 6)
                        layoutParams = FrameLayout.LayoutParams(dotSize, dotSize).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    addView(dot)
                }
                AppSettings.BubbleIndicator.BOLT -> {
                    val text = TextView(this@QuickDrawOverlayService).apply {
                        text = "⚡"
                        setTextColor(0xFFFFFFFF.toInt())
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, if (size == AppSettings.BubbleSize.SMALL) 11f else 13f)
                        gravity = Gravity.CENTER
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    addView(text)
                }
                AppSettings.BubbleIndicator.DRAW -> {
                    val text = TextView(this@QuickDrawOverlayService).apply {
                        text = "✏️"
                        setTextColor(0xFFFFFFFF.toInt())
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, if (size == AppSettings.BubbleSize.SMALL) 10f else 12f)
                        gravity = Gravity.CENTER
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER
                        }
                    }
                    addView(text)
                }
                AppSettings.BubbleIndicator.NONE -> {
                    // No indicator view
                }
            }

            var initialY = 0
            var initialTouchY = 0f
            var isMoving = false

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialY = params.y
                        initialTouchY = event.rawY
                        isMoving = false
                        if (AppSettings.isBubbleHapticEnabled(this@QuickDrawOverlayService)) {
                            vibrate(18)
                        }
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dy = event.rawY - initialTouchY
                        if (abs(dy) > 10) {
                            isMoving = true
                            params.y = initialY + dy.toInt()
                            try {
                                windowManager.updateViewLayout(this, params)
                            } catch (e: Exception) {
                                // Ignore layout updates if detached
                            }
                        }
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isMoving) {
                            // Clicked: open drawing pad!
                            showDrawingOverlay()
                        }
                        true
                    }
                    else -> false
                }
            }
        }

        floatingTriggerView = trigger
        try {
            windowManager.addView(trigger, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateFloatingTrigger() {
        if (!Settings.canDrawOverlays(this)) return
        val currentY = triggerParams?.y ?: 0
        floatingTriggerView?.let { oldView ->
            try {
                windowManager.removeView(oldView)
            } catch (e: Exception) {
                // Ignore
            }
            floatingTriggerView = null
        }
        setupFloatingTrigger(savedY = currentY)
    }

    private fun showDrawingOverlay() {
        if (isOverlayVisible || drawingOverlayView != null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        val overlayRoot = FrameLayout(this).apply {
            setBackgroundColor(0xCC070B19.toInt()) // Modern deep indigo transparent backdrop
        }

        // Custom Gesture Canvas View
        lateinit var gestureCanvas: GestureCanvasView

        // Status Feedback Toast in center/bottom
        val statusText = TextView(this).apply {
            text = "Narysuj gest (możesz odrywać rękę, np. dla liter T, X, +)"
            setTextColor(0xFFE2E8F0.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            setPadding(dpToPx(16), dpToPx(10), dpToPx(16), dpToPx(10))
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(20).toFloat()
                setColor(0xEE0D1322.toInt())
                setStroke(dpToPx(1), 0x4400E5FF.toInt())
            }
            val statusParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dpToPx(50)
            }
            layoutParams = statusParams
        }

        gestureCanvas = GestureCanvasView(
            context = this,
            onInterimStroke = { strokeCount ->
                statusText.text = "Oderwano palec (kreska $strokeCount) • Możesz dorysować kolejną..."
                statusText.setTextColor(0xFF00E5FF.toInt())
            },
            onStrokeComplete = { points ->
                if (points.size >= 3) {
                    performGestureRecognition(points, statusText)
                }
            }
        )

        // Top bar container
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dpToPx(16), dpToPx(48), dpToPx(16), dpToPx(16))
            gravity = Gravity.CENTER_VERTICAL

            val titleView = TextView(this@QuickDrawOverlayService).apply {
                text = "⚡ QuickDraw"
                setTextColor(0xFF00E5FF.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            addView(titleView)

            val clearButton = TextView(this@QuickDrawOverlayService).apply {
                text = "WYCZYŚĆ"
                setTextColor(0xCCFFFFFF.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(14).toFloat()
                    setColor(0x22FFFFFF.toInt())
                }
                setOnClickListener {
                    gestureCanvas.clearCanvas()
                    statusText.text = "Narysuj gest (możesz odrywać rękę)"
                    statusText.setTextColor(0xFFE2E8F0.toInt())
                }
            }
            addView(clearButton)

            val spacer = View(this@QuickDrawOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(8), 1)
            }
            addView(spacer)

            val doneButton = TextView(this@QuickDrawOverlayService).apply {
                text = "✓ WYKONAJ"
                setTextColor(0xFF000000.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(14).toFloat()
                    setColor(0xFF00E676.toInt())
                }
                setOnClickListener {
                    gestureCanvas.forceComplete()
                }
            }
            addView(doneButton)

            val spacer2 = View(this@QuickDrawOverlayService).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(8), 1)
            }
            addView(spacer2)

            val closeButton = TextView(this@QuickDrawOverlayService).apply {
                text = "✕"
                setTextColor(0xCCFFFFFF.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(dpToPx(10), dpToPx(6), dpToPx(10), dpToPx(6))
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(14).toFloat()
                    setColor(0x22FFFFFF.toInt())
                }
                setOnClickListener {
                    hideDrawingOverlay()
                }
            }
            addView(closeButton)
        }

        overlayRoot.addView(gestureCanvas, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        overlayRoot.addView(topBar, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT))
        overlayRoot.addView(statusText)

        drawingOverlayView = overlayRoot
        isOverlayVisible = true

        try {
            windowManager.addView(overlayRoot, overlayParams)
        } catch (e: Exception) {
            e.printStackTrace()
            isOverlayVisible = false
        }
    }

    private fun performGestureRecognition(points: List<GesturePoint>, statusView: TextView) {
        val recognition = GestureRecognizer.recognize(points, cachedGestures)
        val matched = recognition.matchedGesture

        if (matched != null) {
            // Success match!
            vibrate(50)
            val scorePercent = (recognition.bestScore * 100).toInt()
            statusView.text = "✓ Dopasowano: ${matched.name} ($scorePercent%)"
            statusView.setTextColor(0xFF00E676.toInt())

            mainHandler.postDelayed({
                hideDrawingOverlay()
                ActionExecutor.execute(
                    applicationContext,
                    matched.getActionTypeEnum(),
                    matched.actionTarget,
                    matched.actionLabel
                )
            }, 300)
        } else {
            // No match
            vibrate(100)
            val bestCandidate = recognition.topCandidates.firstOrNull()
            if (bestCandidate != null) {
                val scorePercent = (bestCandidate.score * 100).toInt()
                val reqPercent = (bestCandidate.gesture.sensitivity * 100).toInt()
                statusView.text = "Brak dopasowania (Najbliżej: ${bestCandidate.gesture.name} $scorePercent% / $reqPercent%)"
            } else {
                statusView.text = "Nie rozpoznano gestu. Spróbuj ponownie!"
            }
            statusView.setTextColor(0xFFFF5252.toInt())
        }
    }

    private fun hideDrawingOverlay() {
        if (!isOverlayVisible || drawingOverlayView == null) return
        try {
            windowManager.removeView(drawingOverlayView)
        } catch (e: Exception) {
            // Ignored
        } finally {
            drawingOverlayView = null
            isOverlayVisible = false
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action == ACTION_UPDATE_BUBBLE) {
            updateFloatingTrigger()
            return START_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        hideDrawingOverlay()
        floatingTriggerView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            floatingTriggerView = null
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    companion object {
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_UPDATE_BUBBLE = "com.example.action.UPDATE_BUBBLE"
        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, QuickDrawOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, QuickDrawOverlayService::class.java)
            context.stopService(intent)
        }

        fun updateBubble(context: Context) {
            if (!isRunning) return
            try {
                val intent = Intent(context, QuickDrawOverlayService::class.java).apply {
                    action = ACTION_UPDATE_BUBBLE
                }
                context.startService(intent)
            } catch (e: Exception) {
                // Ignore if service not accessible
            }
        }
    }

    // Inner custom touch drawing canvas with multi-stroke & delay support
    private class GestureCanvasView(
        context: Context,
        private val onInterimStroke: (strokeCount: Int) -> Unit,
        private val onStrokeComplete: (List<GesturePoint>) -> Unit
    ) : View(context) {

        private val points = mutableListOf<GesturePoint>()
        private val drawPath = Path()
        private var currentStrokeId = 0
        private var minX = Float.MAX_VALUE
        private var maxX = -Float.MAX_VALUE
        private var minY = Float.MAX_VALUE
        private var maxY = -Float.MAX_VALUE

        private val strokeHandler = Handler(Looper.getMainLooper())
        private val strokeDelayMs = AppSettings.getStrokeDelayMs(context)

        private val finishRunnable = Runnable {
            if (points.size >= 3) {
                val copy = points.toList()
                clearInternal()
                onStrokeComplete(copy)
            } else {
                clearInternal()
            }
        }

        fun forceComplete() {
            strokeHandler.removeCallbacks(finishRunnable)
            if (points.size >= 3) {
                val copy = points.toList()
                clearInternal()
                onStrokeComplete(copy)
            }
        }

        fun clearCanvas() {
            strokeHandler.removeCallbacks(finishRunnable)
            clearInternal()
        }

        private fun clearInternal() {
            points.clear()
            drawPath.reset()
            currentStrokeId = 0
            minX = Float.MAX_VALUE
            maxX = -Float.MAX_VALUE
            minY = Float.MAX_VALUE
            maxY = -Float.MAX_VALUE
            invalidate()
        }

        private val rainbowColors = intArrayOf(
            0xFFFF1744.toInt(), // Red
            0xFFFF6D00.toInt(), // Orange
            0xFFFFD600.toInt(), // Yellow
            0xFF00E676.toInt(), // Green
            0xFF00E5FF.toInt(), // Cyan
            0xFF2979FF.toInt(), // Blue
            0xFF7C4DFF.toInt(), // Violet
            0xFFFF00D4.toInt()  // Magenta
        )

        private val glowPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 26f
            alpha = 120
        }

        private val mainPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 10f
        }

        private val corePaint = Paint().apply {
            color = 0xFFFFFFFF.toInt()
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 3f
            alpha = 220
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            if (!drawPath.isEmpty) {
                val startX = if (minX == Float.MAX_VALUE) 0f else minX
                val startY = if (minY == Float.MAX_VALUE) 0f else minY
                val endX = if (maxX - minX < 30f) startX + 100f else maxX
                val endY = if (maxY - minY < 30f) startY + 100f else maxY

                val rainbowShader = android.graphics.LinearGradient(
                    startX, startY, endX, endY,
                    rainbowColors, null, android.graphics.Shader.TileMode.CLAMP
                )
                glowPaint.shader = rainbowShader
                mainPaint.shader = rainbowShader

                canvas.drawPath(drawPath, glowPaint)
                canvas.drawPath(drawPath, mainPaint)
                canvas.drawPath(drawPath, corePaint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    strokeHandler.removeCallbacks(finishRunnable)
                    if (points.isEmpty()) {
                        currentStrokeId = 0
                        drawPath.reset()
                        drawPath.moveTo(x, y)
                        minX = x
                        maxX = x
                        minY = y
                        maxY = y
                    } else {
                        // User lifted hand and is now drawing the next stroke of the gesture!
                        currentStrokeId++
                        drawPath.moveTo(x, y)
                    }
                    points.add(GesturePoint(x, y, currentStrokeId))
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    drawPath.lineTo(x, y)
                    points.add(GesturePoint(x, y, currentStrokeId))
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    points.add(GesturePoint(x, y, currentStrokeId))
                    invalidate()
                    onInterimStroke(currentStrokeId + 1)
                    strokeHandler.postDelayed(finishRunnable, strokeDelayMs)
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
    }
}
