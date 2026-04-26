package com.example.shilpakala

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var tiltNorm      = 0f
    private var hintText      = ""
    private var isLevel       = true
    private var placeholderTx = "Place product here"

    fun updateTilt(tilt: Float, hint: String, isLevel: Boolean) {
        this.tiltNorm = tilt
        this.hintText = hint
        this.isLevel  = isLevel
        invalidate()
    }

    /** Called from MainActivity.applyLanguage() so the in-frame label switches too */
    fun setPlaceholderText(text: String) {
        placeholderTx = text
        invalidate()
    }

    // ---- Paints ----

    private val dimPaint = Paint().apply {
        color = Color.argb(140, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private val bracketPaint = Paint().apply {
        style       = Paint.Style.STROKE
        strokeWidth = 7f
        isAntiAlias = true
        strokeCap   = Paint.Cap.SQUARE
    }

    private val gridPaint = Paint().apply {
        color       = Color.argb(55, 255, 255, 255)
        style       = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }

    private val levelTrackPaint = Paint().apply {
        color       = Color.argb(100, 255, 255, 255)
        style       = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val bubblePaint = Paint().apply {
        style       = Paint.Style.FILL
        isAntiAlias = true
    }

    private val centreMarkPaint = Paint().apply {
        color       = Color.WHITE
        style       = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val hintTextPaint = Paint().apply {
        textSize    = 38f
        isAntiAlias = true
        typeface    = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign   = Paint.Align.CENTER
    }

    private val frameLabelPaint = Paint().apply {
        color       = Color.argb(180, 255, 255, 255)
        textSize    = 32f
        isAntiAlias = true
        textAlign   = Paint.Align.CENTER
    }

    // ---- Draw ----

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val W = width.toFloat()
        val H = height.toFloat()

        val left   = W * 0.08f
        val top    = H * 0.12f
        val right  = W * 0.92f
        val bottom = H * 0.72f
        val fW = right - left
        val fH = bottom - top

        // Dim outside
        canvas.drawRect(0f, 0f, W, top, dimPaint)
        canvas.drawRect(0f, bottom, W, H, dimPaint)
        canvas.drawRect(0f, top, left, bottom, dimPaint)
        canvas.drawRect(right, top, W, bottom, dimPaint)

        // Rule-of-thirds
        val t1x = left + fW / 3f; val t2x = left + 2 * fW / 3f
        val t1y = top  + fH / 3f; val t2y = top  + 2 * fH / 3f
        canvas.drawLine(t1x, top, t1x, bottom, gridPaint)
        canvas.drawLine(t2x, top, t2x, bottom, gridPaint)
        canvas.drawLine(left, t1y, right, t1y, gridPaint)
        canvas.drawLine(left, t2y, right, t2y, gridPaint)

        // Brackets — green when level, gold when tilted
        val bracketColor = if (isLevel) Color.parseColor("#5CCC5C") else Color.parseColor("#C9A84C")
        bracketPaint.color = bracketColor
        val arm = fW * 0.11f

        canvas.drawLine(left, top + arm, left, top, bracketPaint)
        canvas.drawLine(left, top, left + arm, top, bracketPaint)
        canvas.drawLine(right - arm, top, right, top, bracketPaint)
        canvas.drawLine(right, top, right, top + arm, bracketPaint)
        canvas.drawLine(left, bottom - arm, left, bottom, bracketPaint)
        canvas.drawLine(left, bottom, left + arm, bottom, bracketPaint)
        canvas.drawLine(right - arm, bottom, right, bottom, bracketPaint)
        canvas.drawLine(right, bottom, right, bottom - arm, bracketPaint)

        // In-frame placeholder label (language-aware)
        canvas.drawText(placeholderTx, W / 2f, top + 52f, frameLabelPaint)

        // Level bar
        val barY     = bottom + 56f
        val barLeft  = W * 0.20f
        val barRight = W * 0.80f
        val barLen   = barRight - barLeft

        canvas.drawLine(barLeft, barY, barRight, barY, levelTrackPaint)
        canvas.drawLine(W / 2f, barY - 10f, W / 2f, barY + 10f, centreMarkPaint)

        val bubbleX = (W / 2f + tiltNorm * barLen * 0.45f).coerceIn(barLeft, barRight)
        bubblePaint.color = if (isLevel) Color.parseColor("#5CCC5C") else Color.parseColor("#FF8C42")
        canvas.drawCircle(bubbleX, barY, 14f, bubblePaint)

        // Tilt hint
        if (hintText.isNotEmpty()) {
            hintTextPaint.color = if (isLevel) Color.parseColor("#5CCC5C") else Color.WHITE
            canvas.drawText(hintText, W / 2f, barY + 50f, hintTextPaint)
        }
    }
}