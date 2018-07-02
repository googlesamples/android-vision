/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tensorflow.detector.spc;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import tensorflow.detector.spc.env.BorderedText;
import tensorflow.detector.spc.env.ImageUtils;
import tensorflow.detector.spc.env.Logger;
import tensorflow.detector.spc.tracking.MultiBoxTracker;

import com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity;
import com.google.android.gms.samples.vision.face.facetracker.R;

import org.opencv.android.facetracker.OpenCvActivity;

public class CameraActivityMainALPReu extends Activity
    implements OnImageAvailableListener, Camera.PreviewCallback {

  private static final float MINIMUM_CONFIDENCE = 0.5f;
  static final String RUNTIME_DATA_DIR_ASSET = "runtime_data";
  static final String ANDROID_DATA_DIR = "/data/data/com.google.android.gms.samples.vision.face.facetracker";
  static final String OPENALPR_CONF_FILE = "openalpr.defaults.conf";
  static final String PREF_INSTALLED_KEY = "installed";

  private static Size DESIRED_PREVIEW_SIZE;

  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;

  private Integer sensorOrientation;

  private OpenALPRDetector detector;

  private long lastProcessingTimeMs;
  private long allProcessingTimeMs=0;
  private long countOfRuns = 0;

  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private byte[] luminanceCopy;


  private static final Logger LOGGER = new Logger();

  private static final int PERMISSIONS_REQUEST = 1;

  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  private static final String PERMISSION_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

  private boolean debug = false;

  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;

  protected int previewWidth = 0;
  protected int previewHeight = 0;

  private Runnable postInferenceCallback;
  private Runnable imageConverter;

  private Button mBtnSwitch;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);

    if (!PreferenceManager.getDefaultSharedPreferences(
            getApplicationContext())
            .getBoolean(PREF_INSTALLED_KEY, false)) {

      PreferenceManager.getDefaultSharedPreferences(
              getApplicationContext())
              .edit().putBoolean(PREF_INSTALLED_KEY, true).apply();

            /*PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                    edit().putString(RUNTIME_DATA_DIR_ASSET, ANDROID_DATA_DIR).commit();*/

      copyAssetFolder(getAssets(), RUNTIME_DATA_DIR_ASSET,
              ANDROID_DATA_DIR + File.separatorChar + RUNTIME_DATA_DIR_ASSET);
      LOGGER.d("Assets copied");
    }

    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);
    DESIRED_PREVIEW_SIZE = new Size(metrics.widthPixels, metrics.heightPixels);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.activity_camera_alpr_eu);

    mBtnSwitch = (Button) findViewById(R.id.btnSwitch);

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }
  }

  private byte[] lastPreviewFrame;

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /**
   * Callback for android.hardware.Camera API
   */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    lastPreviewFrame = bytes;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
        new Runnable() {
          @Override
          public void run() {
            ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
          }
        };

    postInferenceCallback =
        new Runnable() {
          @Override
          public void run() {
            camera.addCallbackBuffer(bytes);
            isProcessingFrame = false;
          }
        };
    processImage();
  }

  /**
   * Callback for Camera2 API
   */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    //We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
          new Runnable() {
            @Override
            public void run() {
              ImageUtils.convertYUV420ToARGB8888(
                  yuvBytes[0],
                  yuvBytes[1],
                  yuvBytes[2],
                  previewWidth,
                  previewHeight,
                  yRowStride,
                  uvRowStride,
                  uvPixelStride,
                  rgbBytes);
            }
          };

      postInferenceCallback =
          new Runnable() {
            @Override
            public void run() {
              image.close();
              isProcessingFrame = false;
            }
          };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);

    if (!isFinishing()) {
      LOGGER.d("Requesting finish");
      finish();
    }

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      final int requestCode, final String[] permissions, final int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST) {
      if (grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED
          && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED &&
          checkSelfPermission(PERMISSION_STORAGE) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA) ||
          shouldShowRequestPermissionRationale(PERMISSION_STORAGE)) {
        Toast.makeText(CameraActivityMainALPReu.this,
            "Camera AND storage permission are required for this app", Toast.LENGTH_LONG).show();
      }
      requestPermissions(new String[] {PERMISSION_CAMERA, PERMISSION_STORAGE}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
      CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API = (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
            || isHardwareLevelSupported(characteristics, 
                                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
          CameraConnectionFragment.newInstance(
              new CameraConnectionFragment.ConnectionCallback() {
                @Override
                public void onPreviewSizeChosen(final Size size, final int rotation) {
                  previewHeight = size.getHeight();
                  previewWidth = size.getWidth();
                  CameraActivityMainALPReu.this.onPreviewSizeChosen(size, rotation);
                }
              },
              this,
              R.layout.camera_connection_fragment_tracking,
              DESIRED_PREVIEW_SIZE);

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
          new LegacyCameraConnectionFragment(this,
                  R.layout.camera_connection_fragment_tracking,
                  DESIRED_PREVIEW_SIZE);
    }

    getFragmentManager()
        .beginTransaction()
        .replace(R.id.container, fragment)
        .commit();

    mBtnSwitch.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent myIntent = new Intent(CameraActivityMainALPReu.this, CameraActivityMainALPRus.class);
        CameraActivityMainALPReu.this.startActivity(myIntent);
      }
    });
  }

  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  public void requestRender() {
    final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
    if (overlay != null) {
      overlay.postInvalidate();
    }
  }

  public void addCallback(final OverlayView.DrawCallback callback) {
    final OverlayView overlay = (OverlayView) findViewById(R.id.debug_overlay);
    if (overlay != null) {
      overlay.addCallback(callback);
    }
  }

  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      debug = !debug;
      requestRender();
      onSetDebug(debug);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }

  private BorderedText borderedText;

  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
            TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    tracker = new MultiBoxTracker(this);

    try {
      String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar +
              RUNTIME_DATA_DIR_ASSET + File.separatorChar +OPENALPR_CONF_FILE;
      String openAlprRuntimeDataDir = ANDROID_DATA_DIR + File.separatorChar +
              RUNTIME_DATA_DIR_ASSET;

      detector = OpenALPRDetector.create(openAlprRuntimeDataDir, openAlprConfFile, "eu");
    } catch (final IOException e) {
      LOGGER.e("Exception initializing classifier!", e);
      Toast toast =
              Toast.makeText(
                      getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);

    frameToCropTransform =
            ImageUtils.getTransformationMatrix(
                    previewWidth, previewHeight,
                    previewWidth, previewHeight,
                    sensorOrientation, true);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
            new OverlayView.DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                tracker.draw(canvas);
                if (isDebug()) {
                  tracker.drawDebug(canvas);
                }
              }
            });

    addCallback(
            new OverlayView.DrawCallback() {
              @Override
              public void drawCallback(final Canvas canvas) {
                if (!isDebug()) {
                  return;
                }
                final Bitmap copy = cropCopyBitmap;
                if (copy == null) {
                  return;
                }

            final int backgroundColor = Color.argb(100, 0, 0, 0);
            canvas.drawColor(backgroundColor);

            final Matrix matrix = new Matrix();
            final float scaleFactor = .5f;
            matrix.postScale(scaleFactor, scaleFactor);
            matrix.postTranslate(
                canvas.getWidth() - copy.getWidth() * scaleFactor,
                canvas.getHeight() - copy.getHeight() * scaleFactor);
            canvas.drawBitmap(copy, matrix, new Paint());

            final Vector<String> lines = new Vector<String>();

            lines.add("");
            lines.add("Frame: " + previewWidth + "x" + previewHeight);
            //lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
            lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
            //lines.add("Rotation: " + sensorOrientation);
            lines.add("OpenALPR version: " + detector.getVersion());
            lines.add("Processing time: " + lastProcessingTimeMs + "ms");


            borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines);
              }
            });
  }

  OverlayView trackingOverlay;

  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    byte[] originalLuminance = getLuminance();
    tracker.onFrame(
            previewWidth,
            previewHeight,
            getLuminanceStride(),
            sensorOrientation,
            originalLuminance,
            timestamp);
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    if (luminanceCopy == null) {
      luminanceCopy = new byte[originalLuminance.length];
    }
    System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
            new Runnable() {
              @Override
              public void run() {
                LOGGER.i("Running detection on image " + currTimestamp);
                final long startTime = SystemClock.uptimeMillis();
                final List<Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);

                float minimumConfidence = MINIMUM_CONFIDENCE;

                final List<Recognition> mappedRecognitions =
                        new LinkedList<Recognition>();

                for (final Recognition result : results) {
                  final RectF location = result.getLocation();
                  if (location != null && result.getConfidence() >= minimumConfidence) {
                    canvas.drawRect(location, paint);

                    cropToFrameTransform.mapRect(location);
                    result.setLocation(location);
                    mappedRecognitions.add(result);
                  }
                }

                tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                trackingOverlay.postInvalidate();

                requestRender();
                computingDetection = false;
              }
            });
  }

  private static boolean copyAssetFolder(AssetManager assetManager,
                                         String fromAssetPath, String toPath) {
    try {
      String[] files = assetManager.list(fromAssetPath);
      new File(toPath).mkdirs();
      boolean res = true;
      for (String file : files)
        if (file.contains("."))
          res &= copyAsset(assetManager,
                  fromAssetPath + "/" + file,
                  toPath + "/" + file);
        else
          res &= copyAssetFolder(assetManager,
                  fromAssetPath + "/" + file,
                  toPath + "/" + file);
      return res;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private static boolean copyAsset(AssetManager assetManager,
                                   String fromAssetPath, String toPath) {
    InputStream in = null;
    OutputStream out = null;
    try {
      in = assetManager.open(fromAssetPath);
      new File(toPath).createNewFile();
      out = new FileOutputStream(toPath);
      copyFile(in, out);
      in.close();
      in = null;
      out.flush();
      out.close();
      out = null;
      return true;
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private static void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while((read = in.read(buffer)) != -1){
      out.write(buffer, 0, read);
    }
  }

  public void onSetDebug(final boolean debug) {
    detector.enableStatLogging(debug);
  }
}
