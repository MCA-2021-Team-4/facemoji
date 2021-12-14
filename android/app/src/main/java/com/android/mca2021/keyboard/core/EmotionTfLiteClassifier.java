package com.android.mca2021.keyboard.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

/**
 * Created by avsavchenko.
 */
public class EmotionTfLiteClassifier extends TfLiteClassifier{

    /** Tag for the {@link Log}. */
    private static final String TAG = "EmotionTfLite";

    private static final String MODEL_FILE = "emotions_mobilenet.tflite";
    private static float[] weight = {1f, 1f, 1f, 1f, 1f, 1f, 1f};
    private static float maxWeight = 3f;
    private final SharedPreferences sharedPreferences;

    public EmotionTfLiteClassifier(final Context context) throws IOException {
        super(context,MODEL_FILE);
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
    }

    private void setEmojiWeight(SharedPreferences sharedPreferences) {
        weight[0] = 1f + (maxWeight - 1) * sharedPreferences.getInt("anger", 4) / 8f;
        weight[1] = 1f + (maxWeight - 1) * sharedPreferences.getInt("disgust", 4) / 8f;
        weight[2] = 1f + (maxWeight - 1) * sharedPreferences.getInt("fear", 4) / 8f;
        weight[3] = 1f + (maxWeight - 1) * sharedPreferences.getInt("happiness", 4) / 8f;
        weight[4] = 1f + (maxWeight - 1) * sharedPreferences.getInt("neutral", 4) / 8f;
        weight[5] = 1f + (maxWeight - 1) * sharedPreferences.getInt("sadness", 4) / 8f;
        weight[6] = 1f + (maxWeight - 1) * sharedPreferences.getInt("surprise", 4) / 8f;
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
        setEmojiWeight(sharedPreferences);
        return res;
    }
}
