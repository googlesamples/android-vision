package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.google.android.gms.samples.vision.face.facetracker", appContext.getPackageName());
    }


    @Rule
    public ActivityTestRule<FaceTrackerActivity> activityRule = new ActivityTestRule(FaceTrackerActivity.class);

    @Test
    public void callAddContext()  {
        FaceTrackerActivity activity  = activityRule.getActivity();
//        int res = activity.nativeAdd(1,2);
//        assertEquals(3, res);
    }
}
