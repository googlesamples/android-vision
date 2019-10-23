package com.google.android.gms.samples.vision.face.facetracker.ui.face.tracker;

import com.google.android.gms.samples.vision.face.facetracker.ui.face.graph.FaceGraphic;
import com.google.android.gms.samples.vision.face.facetracker.ui.face.graph.GraphicOverlay;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

public class GraphicFaceTracker extends Tracker<Face> {

    private final GraphicOverlay mOverlay;
    private final FaceGraphic mFaceGraphic;
    private final GraphicFaceTrackerFactory.IFaceItamCallback mFaceItemCallback;

    GraphicFaceTracker(GraphicOverlay overlay, GraphicFaceTrackerFactory.IFaceItamCallback faceItamCallback) {
        mOverlay = overlay;
        mFaceItemCallback = faceItamCallback;
        mFaceGraphic = new FaceGraphic(overlay);
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
        mFaceGraphic.setId(faceId);
        mFaceItemCallback.onNewItem(item);
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mFaceGraphic);
        mFaceGraphic.updateFace(face);
        mFaceItemCallback.onUpdate(face);
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mFaceGraphic);
        mFaceItemCallback.onMissing(mFaceGraphic.getFace());
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        mOverlay.remove(mFaceGraphic);
        mFaceItemCallback.onDone(mFaceGraphic.getFace());
    }
}
