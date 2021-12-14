package com.android.mca2021.keyboard.core;

import android.util.Log;


import java.io.Serializable;


/**
 * Created by avsavchenko.
 */
public class EmotionData implements ClassifierResult,Serializable {
    public float[] emotionScores;
    private static float[] weightArray = {1f, 1f, 1f, 1f, 1f, 1f, 1f};

    public EmotionData(float[] emotionScores){
        this.emotionScores = new float[emotionScores.length];
        System.arraycopy(emotionScores, 0, this.emotionScores, 0, emotionScores.length);
    }

    private static String[] emotions={"Anger", "Disgust", "Fear", "Happiness", "Neutral", "Sadness", "Surprise"};
    public static String getEmotion(float[] emotionScores){
        for (int i = 0; i < 7; i++) {
            emotionScores[i] *= weightArray[i];
        }
        String emotionScoreStr = String.format("Anger: %f, Disgust: %f, Fear: %f, Happiness: %f, Neutral: %f, Sadness: %f, Surprise: %f",
                emotionScores[0], emotionScores[1], emotionScores[2], emotionScores[3], emotionScores[4], emotionScores[5],
                emotionScores[6]);
        Log.i("EmotionScore", emotionScoreStr);

        int bestInd=-1;
        if (emotionScores!=null){
            float maxScore=0;
            for(int i=0;i<emotionScores.length;++i){
                if(maxScore<emotionScores[i]){
                    maxScore=emotionScores[i];
                    bestInd=i;
                }
            }
        }
        return emotions[bestInd];
    }
    public String toString(){
        return getEmotion(emotionScores);
    }
    public void changeWeight(float[] newWeightArray) {
        weightArray = newWeightArray;
    }
}
