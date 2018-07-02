package com.openalpr.jni;

import com.openalpr.jni.json.JSONException;

import tensorflow.detector.spc.env.Logger;

public class Alpr {
    private static final Logger LOGGER = new Logger();
    static {
        // Load the OpenALPR library at runtime
        // openalprjni.dll (Windows) or libopenalprjni.so (Linux/Mac)
        System.loadLibrary("openalprjni");
    }

    private native void initialize(String country, String configFile, String runtimeDir);
    private native void dispose();

    private native boolean isloaded();
    private native String nativeRecognize(String imageFile);
    private native String nativeRecognize(byte[] imageBytes);
    private native String nativeRecognize(long imageData, int bytesPerPixel, int imgWidth, int imgHeight);

    private native void setdefaultregion(String region);
    private native void detectregion(boolean detectRegion);
    private native void settopn(int topN);
    private native String getversion();



    public Alpr(String country, String configFile, String runtimeDir)
    {
        initialize(country, configFile, runtimeDir);
    }

    public void unload()
    {
        dispose();
    }

    public boolean isLoaded()
    {
        return isloaded();
    }

    public AlprResults recognize(String imageFile) throws AlprException
    {
        try {
            String json = nativeRecognize(imageFile);
            LOGGER.d("====================Recieve results");
            return new AlprResults(json);
        } catch (JSONException e)
        {
            throw new AlprException("Unable to parse ALPR results");
        }
    }


    public AlprResults recognize(byte[] imageBytes) throws AlprException
    {
        try {
            String json = nativeRecognize(imageBytes);
            return new AlprResults(json);
        } catch (JSONException e)
        {
            throw new AlprException("Unable to parse ALPR results");
        }
    }


    public AlprResults recognize(long imageData, int bytesPerPixel, int imgWidth, int imgHeight) throws AlprException
    {
        try {
            String json = nativeRecognize(imageData, bytesPerPixel, imgWidth, imgHeight);
            return new AlprResults(json);
        } catch (JSONException e)
        {
            throw new AlprException("Unable to parse ALPR results");
        }
    }


    public void setTopN(int topN)
    {
        settopn(topN);
    }

    public void setDefaultRegion(String region)
    {
        setdefaultregion(region);
    }

    public void setDetectRegion(boolean detectRegion)
    {
        detectregion(detectRegion);
    }

    public String getVersion()
    {
        return getversion();
    }
}
