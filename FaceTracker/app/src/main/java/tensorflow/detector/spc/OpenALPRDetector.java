/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package tensorflow.detector.spc;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Trace;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprCoordinate;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprRegionOfInterest;
import com.openalpr.jni.AlprResults;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import java.io.File;

import tensorflow.detector.spc.env.Logger;

public class OpenALPRDetector {
  private static final Logger LOGGER = new Logger();

  private float[] outputLocations;
  private float[] outputScores;
  private byte[] byteValues = null;

  private Alpr alpr;
  private boolean logStats = false;
  private String resultLine = "";


  public static OpenALPRDetector create(String runtimeDataDir, String openAlprConfFile) throws IOException {
    final OpenALPRDetector d = new OpenALPRDetector();
    d.alpr = new Alpr("eu", openAlprConfFile, runtimeDataDir);
    d.alpr.setTopN(1);
    d.alpr.setDefaultRegion("");
    LOGGER.d("=============================Is LOADED" + d.alpr.isLoaded());
    LOGGER.d("OpenALPR Version: " + d.alpr.getVersion());

    return d;
  }

  private OpenALPRDetector() {}

  public List<Recognition> recognizeImage(final Bitmap bitmap) {
    final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
    //String line = "";
    if(byteValues == null) {
      byteValues = new byte[bitmap.getWidth() * bitmap.getHeight() * 3];
    }

    byteValues = getBytesFromBitmap(bitmap);
    try {
      LOGGER.d("=========================Start");
      LOGGER.d("OpenALPR Version: " + alpr.getVersion());
      AlprResults results = alpr.recognize(byteValues);
      LOGGER.d("=========================Finish");
      LOGGER.d("OpenALPR Version: " + alpr.getVersion());

      resultLine = "OpenALPR Version: " + alpr.getVersion() +
              " Processing Time: " + results.getTotalProcessingTimeMs() + " ms";

      LOGGER.d("OpenALPR Version: " + alpr.getVersion());
      LOGGER.d("Image Size: " + results.getImgWidth() + "x" + results.getImgHeight());
      LOGGER.d("Processing Time: " + results.getTotalProcessingTimeMs() + " ms");
      LOGGER.d("Found " + results.getPlates().size() + " results");
      LOGGER.d("Found " + results.getRegionsOfInterest().size() + " rois");

      LOGGER.d("  %-15s%-8s\n", "Plate Number", "Confidence");
      int i = 0;
      List<AlprRegionOfInterest> rois = results.getRegionsOfInterest();
      if(results.getPlates().size() == 0)
        resultLine = resultLine + " Nothing was found.";
      for (AlprPlateResult result : results.getPlates())
        {
            AlprPlate bestPlate = result.getBestPlate();
            List<AlprCoordinate> coords = result.getPlatePoints();

            for(AlprCoordinate coord : coords)
              LOGGER.d("    coord x: %d, y: %d", coord.getX(), coord.getY());

            final RectF detection =
                  new RectF(
                          coords.get(0).getX(),
                          coords.get(0).getY(),
                          coords.get(2).getX(),
                          coords.get(2).getY());
            Recognition recognition = new Recognition("" + i, bestPlate.getCharacters(), bestPlate.getOverallConfidence(), detection);
            recognitions.add(recognition);
            LOGGER.d("%-15s%-2f\n", bestPlate.getCharacters(), bestPlate.getOverallConfidence());
            resultLine = resultLine + " Found : " + bestPlate.getCharacters();
            LOGGER.d("Roi: %d, %d, %d, %d\n", rois.get(i).getX(), rois.get(i).getY(), rois.get(i).getWidth(), rois.get(i).getHeight());
            i += 1;
        }
    }catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      resultLine = resultLine + " Error in detecting and recognizing a plate.";
      return recognitions;
    }

    return recognitions;
  }

  public byte[] getBytesFromBitmap(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    return stream.toByteArray();
  }

    public void close() {
        alpr.unload();
    }

  public String getResultInformation(){
    return resultLine;
  }

  public void enableStatLogging(final boolean logStats) {
    this.logStats = logStats;
  }

  public String getStatString() {
    return "";
  }

}
