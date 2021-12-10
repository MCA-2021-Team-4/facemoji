package com.android.mca2021.keyboard.core;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * Created by avsavchenko.
 */
public class EmotionTfLiteClassifier extends TfLiteClassifier{

    /** Tag for the {@link Log}. */
    private static final String TAG = "EmotionTfLite";

    private static final String MODEL_FILE = "emotions_mobilenet.tflite";
    public static float weight = 1;
    public EmotionTfLiteClassifier(final Context context) throws IOException {
        super(context,MODEL_FILE);
    }

    public void changeWeight(float newWeight) {
        weight = newWeight;
    }

    protected void addPixelValue(int val) {
        imgData.putFloat((val & 0xFF) - 103.939f);
        imgData.putFloat(((val >> 8) & 0xFF) - 116.779f);
        imgData.putFloat(((val >> 16) & 0xFF) - 123.68f);
    }

    protected ClassifierResult getResults(float[][][] outputs) {
        final float[] emotions_scores = outputs[0][0];
        EmotionData res=new EmotionData(emotions_scores);
        res.changeWeight(weight);
        Log.i("Weight in EmotionCf", String.valueOf(weight));
        return res;
    }
}
