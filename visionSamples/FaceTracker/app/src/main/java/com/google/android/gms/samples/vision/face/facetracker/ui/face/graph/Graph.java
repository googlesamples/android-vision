package com.google.android.gms.samples.vision.face.facetracker.ui.face.graph;

import android.graphics.Canvas;

import com.google.android.gms.vision.CameraSource;

/**
 * Base class for a custom graphics object to be rendered within the graphic overlay.  Subclass
 * this and implement the {@link Graphic#draw(Canvas)} method to define the
 * graphics element.  Add instances to the overlay using {@link GraphicOverlay#add(Graphic)}.
 */
abstract class Graphic {
    private GraphicOverlay mOverlay;

    public Graphic(GraphicOverlay overlay) {
        mOverlay = overlay;
    }

    /**
     * Draw the graphic on the supplied canvas.  Drawing should use the following methods to
     * convert to view coordinates for the graphics that are drawn:
     * <ol>
     * <li>{@link Graphic#scaleX(float)} and {@link Graphic#scaleY(float)} adjust the size of
     * the supplied value from the preview scale to the view scale.</li>
     * <li>{@link Graphic#translateX(float)} and {@link Graphic#translateY(float)} adjust the
     * coordinate from the preview's coordinate system to the view coordinate system.</li>
     * </ol>
     *
     * @param canvas drawing canvas
     */
    public abstract void draw(Canvas canvas);

    /**
     * Adjusts a horizontal value of the supplied value from the preview scale to the view
     * scale.
     */
    public float scaleX(float horizontal) {
        return horizontal * mOverlay.mWidthScaleFactor;
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    public float scaleY(float vertical) {
        return vertical * mOverlay.mHeightScaleFactor;
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateX(float x) {
        if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
            return mOverlay.getWidth() - scaleX(x);
        } else {
            return scaleX(x);
        }
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateY(float y) {
        return scaleY(y);
    }

    public void postInvalidate() {
        mOverlay.postInvalidate();
    }
}
