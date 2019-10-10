/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay
import com.google.android.gms.vision.text.Text
import com.google.android.gms.vision.text.TextBlock

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class OcrGraphic internal constructor(overlay: GraphicOverlay<*>, val textBlock: TextBlock?) : GraphicOverlay.Graphic(overlay) {

    var id: Int = 0

    init {

        if (rectPaint == null) {
            rectPaint = Paint()
            rectPaint!!.color = TEXT_COLOR
            rectPaint!!.style = Paint.Style.STROKE
            rectPaint!!.strokeWidth = 4.0f
        }

        if (textPaint == null) {
            textPaint = Paint()
            textPaint!!.color = TEXT_COLOR
            textPaint!!.textSize = 54.0f
        }
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /**
     * Checks whether a point is within the bounding box of this graphic.
     * The provided point should be relative to this graphic's containing overlay.
     * @param x An x parameter in the relative context of the canvas.
     * @param y A y parameter in the relative context of the canvas.
     * @return True if the provided point is contained within this graphic's bounding box.
     */
    override fun contains(x: Float, y: Float): Boolean {
        // TODO: Check if this graphic's text contains this point.
        if (textBlock == null) {
            return false
        }
        var rect = RectF(textBlock.boundingBox)
        rect = translateRect(rect)
        return rect.contains(x, y)
    }

    /**
     * Draws the text block annotations for position, size, and raw value on the supplied canvas.
     */
    override fun draw(canvas: Canvas) {
        // TODO: Draw the text onto the canvas.
        if (textBlock == null) {
            return
        }

        // Draws the bounding box around the TextBlock.
        var rect = RectF(textBlock.boundingBox)
        rect = translateRect(rect)
        canvas.drawRect(rect, rectPaint!!)

        // Render the text at the bottom of the box.
        //        canvas.drawText(text.getValue(), rect.left, rect.bottom, textPaint);
        val textComponents = textBlock.components
        for (currentText in textComponents) {
            val left = translateX(currentText.boundingBox.left.toFloat())
            val bottom = translateY(currentText.boundingBox.bottom.toFloat())
            canvas.drawText(currentText.value, left, bottom, textPaint!!)
        }
    }

    companion object {

        private val TEXT_COLOR = Color.WHITE

        private var rectPaint: Paint? = null
        private var textPaint: Paint? = null
    }
}
