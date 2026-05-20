package com.example.memoir.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator

class OverlayHandleView(context: Context) : View(context) {

    var onSwipeTriggered: (() -> Unit)? = null
    
    var isDarkMode = false
        set(value) {
            field = value
            updateColors()
            invalidate()
        }

    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val handleWidthResting = 6f * resources.displayMetrics.density
    private val handleWidthActive = 8f * resources.displayMetrics.density
    private val handleHeight = 60f * resources.displayMetrics.density
    
    private val threshold = 20f * resources.displayMetrics.density // distance to trigger swipe
    private val maxDrag = 35f * resources.displayMetrics.density // max drag distance
    
    private var initialX = 0f
    private var initialY = 0f
    private var isDragging = false
    private var dragDistance = 0f
    private var isActivated = false
    private var hasVibrated = false

    init {
        updateColors()
    }

    private fun updateColors() {
        if (isActivated) {
            // Armed state: Solid black/white depending on theme
            handlePaint.color = if (isDarkMode) 0xFFF8F8F8.toInt() else 0xFF111111.toInt()
        } else {
            // Idle state: Visible gray
            handlePaint.color = if (isDarkMode) 0xFFCCCCCC.toInt() else 0xFF777777.toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the handle bar on the right edge of the view (which is 48dp wide)
        // Resting position is right aligned, e.g. 4dp from the right edge
        val rightPadding = 4f * resources.displayMetrics.density
        
        // Calculate dimensions based on drag state
        val currentWidth = if (isActivated) handleWidthActive else handleWidthResting
        
        // Slide the handle to the left by dragDistance
        val handleRight = width.toFloat() - rightPadding - dragDistance
        val handleLeft = handleRight - currentWidth
        val handleTop = (height - handleHeight) / 2f
        val handleBottom = (height + handleHeight) / 2f
        val radius = currentWidth / 2f
        
        canvas.drawRoundRect(
            handleLeft, handleTop, handleRight, handleBottom,
            radius, radius, handlePaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.rawX
                initialY = event.rawY
                isDragging = true
                isActivated = false
                hasVibrated = false
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val deltaX = initialX - event.rawX // Swipe left is positive
                    dragDistance = deltaX.coerceIn(0f, maxDrag)
                    
                    val wasActivated = isActivated
                    isActivated = dragDistance >= threshold
                    
                    if (isActivated != wasActivated) {
                        updateColors()
                        if (isActivated && !hasVibrated) {
                            triggerVibration()
                            hasVibrated = true
                        } else if (!isActivated) {
                            hasVibrated = false
                        }
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    isDragging = false
                    if (isActivated) {
                        onSwipeTriggered?.invoke()
                    }
                    animateBack()
                }
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun animateBack() {
        val startDrag = dragDistance
        val animator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 180
            interpolator = OvershootInterpolator(0.8f)
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                dragDistance = startDrag * fraction
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dragDistance = 0f
                    isActivated = false
                    updateColors()
                    invalidate()
                }
            })
        }
        animator.start()
    }
    
    private fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(20)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
