package dlib.android;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class FaceRecognizerTest {
    @Test
    public void RecognizeTest() {
        FaceRecognizer fr = new FaceRecognizer();
        String res = fr.Recognize();
        assertEquals("Unknown", res);
    }
}
