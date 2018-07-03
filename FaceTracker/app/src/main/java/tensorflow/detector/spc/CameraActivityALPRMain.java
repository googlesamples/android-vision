package tensorflow.detector.spc;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import tensorflow.detector.spc.OpenALPRDetector;
import tensorflow.detector.spc.env.Logger;

import com.google.android.gms.samples.vision.face.facetracker.R;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class CameraActivityALPRMain extends Activity {

  private static final int REQUEST_IMAGE = 100;

  static final String RUNTIME_DATA_DIR_ASSET = "runtime_data";
  static final String ANDROID_DATA_DIR = "/data/data/com.google.android.gms.samples.vision.face.facetracker";
  static final String OPENALPR_CONF_FILE = "openalpr.defaults.conf";
  static final String PREF_INSTALLED_KEY = "installed";

  private static File destination;
  private TextView resultTextView;
  private ImageView imageView;

  private Button button, buttonDetect, buttonTake;
  private String imgPath = null;
  private Bitmap bmp;
  private ImageView imOne;

  private OpenALPRDetector alprDetector;
  private static final Logger LOGGER = new Logger();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_alpr);

    button = (Button) findViewById(R.id.button);
    buttonDetect = (Button) findViewById(R.id.buttonDetect);

    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
          Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          startActivityForResult(i, REQUEST_IMAGE);
      }
    });
    resultTextView = (TextView) findViewById(R.id.textView);

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
      LOGGER.d("=============================Assets copied");
    }
    try {
      String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar +
              RUNTIME_DATA_DIR_ASSET + File.separatorChar +OPENALPR_CONF_FILE;
      String openAlprRuntimeDataDir = ANDROID_DATA_DIR + File.separatorChar +
              RUNTIME_DATA_DIR_ASSET;

      alprDetector = OpenALPRDetector.create(openAlprRuntimeDataDir, openAlprConfFile, "eu");
    }catch(final Exception e){
      LOGGER.e("Exception initializing alpr detector!", e);
    }

    buttonDetect.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
            final List<Recognition> results = alprDetector.recognizeImage(bmp);
            resultTextView.setText(alprDetector.getResultInformation());
      }
    });

    imOne = (ImageView) findViewById(R.id.imageView);
    buttonDetect.setEnabled(false);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {

      Uri selectedImage = data.getData();
      String[] filePathColumn = {MediaStore.Images.Media.DATA};
      Cursor cursor = CameraActivityALPRMain.this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      imgPath = cursor.getString(columnIndex);
      cursor.close();

      bmp = BitmapFactory.decodeFile(imgPath);
      imOne.setImageBitmap(bmp);
      resultTextView.setText("Image is selected. Press button to detect.");
      buttonDetect.setEnabled(true);

      super.onActivityResult(requestCode, resultCode, data);
    }
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

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    alprDetector.close();
  }
}
