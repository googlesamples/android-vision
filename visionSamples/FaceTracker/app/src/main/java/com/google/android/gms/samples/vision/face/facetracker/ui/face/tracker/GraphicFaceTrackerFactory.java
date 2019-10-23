package com.google.android.gms.samples.vision.face.facetracker.ui.face.tracker;

import com.google.android.gms.samples.vision.face.facetracker.ui.face.graph.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

/**
 * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
 * uses this factory to create face trackers as needed -- one for each individual.
 */
public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face>  {

    public interface IFaceItamCallback {
        void onNewItem(Face face);
        void onUpdate(Face face);
        void onMissing(Face face);
        void onDone(Face face);
    }

    private final IFaceItamCallback mFaceItemCallback;
    private final GraphicOverlay mOverlay;

    public GraphicFaceTrackerFactory(GraphicOverlay mOverlay, IFaceItamCallback faceItamCallback) {
        this.mOverlay = mOverlay;
        this.mFaceItemCallback = faceItamCallback;
    }

    @Override
    public Tracker<Face> create(Face face) {
        return new GraphicFaceTracker(mOverlay, mFaceItemCallback);
    }
}
