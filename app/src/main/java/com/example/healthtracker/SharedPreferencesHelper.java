package com.example.healthtracker;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "health_tracker_prefs";
    private static final String STEP_COUNT_KEY = "step_count";
    private static final String MOVEMENT_DISTANCE_KEY = "movement_distance";
    private static final String PUSHUP_COUNT_KEY = "pushup_count";
    private static final String DATE_KEY = "date";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveStepCount(int stepCount) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, stepCount);
        editor.apply();
    }

    public int getStepCount(Context context) {
        return sharedPreferences.getInt(STEP_COUNT_KEY, 0);
    }
    public void saveMovementDistance(Context context, float distance) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(MOVEMENT_DISTANCE_KEY, distance);
        editor.apply();
    }
    public void saveMovementDistance(float movementDistance, Context context) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(MOVEMENT_DISTANCE_KEY, movementDistance);
        editor.apply();
    }

    public float getMovementDistance() {
        return sharedPreferences.getFloat(MOVEMENT_DISTANCE_KEY, 0);
    }
    public int getPushupCount() {
        return sharedPreferences.getInt(PUSHUP_COUNT_KEY, 0);
    }

    public void savePushupCount(int pushupCount ) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PUSHUP_COUNT_KEY, pushupCount);
        editor.apply();
    }

    public String getDate(Context context) {
        return sharedPreferences.getString(DATE_KEY, "");
    }

    public void saveDate(String date, Context context) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DATE_KEY, date);
        editor.apply();
    }
    public void resetDataForNewDay(Context context) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(STEP_COUNT_KEY, 0);
        editor.putFloat(MOVEMENT_DISTANCE_KEY, 0);
        editor.putInt(PUSHUP_COUNT_KEY, 0);
        editor.apply();
    }

    public String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
