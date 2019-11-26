**Android Vision API Samples**
==========================

The application use **Mobile Vision API** for a POC usage.   
It will use **ML Kit** as next improvement stage.

Current it offer two application about face detect listed as below:

### **Face Detect Camera**

- FaceTrackerCameraActivity.java

Start the camera as below code snapshot :
```java
Intent intnet = new Intent()
// EXTRA_DEFAULT_FACING(optional) control camera initial is back or front. 0: CAMERA_FACING_BACK(default), 1:CAMERA_FACING_FRONT
intnet.putExtra(EXTRA_DEFAULT_FACING, 0)
// EXTRA_OUTPUT control file captured photo output path. Default would be /sdcard/Android/data/[Application ID]/cache/face_track.jpg
intnet.putExtra(EXTRA_OUTPUT, tempPhotoFile.getAbsolutePath())
startActivityForResoult(intent,FaceTrackerCameraActivity.class)
```  
Receive the captured photo as below :
```java
onActivityResult(int requestCode, int resultCode, Intent data) {
    ...
    boolean isContainFace = datat.getBooleanExtra(EXTRA_IS_CONTAIN_FACE, false)
    String capturedPhotoPath = data.getStringExtra(EXTRA_OUTPUT)
    ... 
    //you can do whatever you want
}
```

![image](https://github.com/YomiRY/android-vision/blob/master/visionSamples/FaceTracker/images/face_track_camera.png)

Face Detect SignIn/SignOut
==========================

![image](https://github.com/YomiRY/android-vision/blob/master/visionSamples/FaceTracker/images/signing_application1.png)

![image](https://github.com/YomiRY/android-vision/blob/master/visionSamples/FaceTracker/images/signing_application2.png)

![image](https://github.com/YomiRY/android-vision/blob/master/visionSamples/FaceTracker/images/signing_application3.png)
