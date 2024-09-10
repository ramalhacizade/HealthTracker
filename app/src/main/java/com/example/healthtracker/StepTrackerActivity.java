package com.example.healthtracker;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class StepTrackerActivity extends AppCompatActivity implements SensorEventListener {

    private SharedPreferencesHelper sharedPreferencesHelper;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isSensorRegistered = false;
    private int stepCount = 0;
    private TextView stepCountTextView;

    private final float[] gravity = new float[3];
    private final float[] linearAcceleration = new float[3];
    private static final float ALPHA = 0.8f;

    private static final float STEP_THRESHOLD = 14.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_tracker);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        stepCountTextView = findViewById(R.id.stepCountTextView);

        stepCount = sharedPreferencesHelper.getStepCount(this);
        updateStepCountUI(stepCount);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        stepCountTextView = findViewById(R.id.stepCountTextView);
        updateStepCountUI(stepCount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null && !isSensorRegistered) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            isSensorRegistered = true;
        }
        showTutorialDialog();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isSensorRegistered) {
            sensorManager.unregisterListener(this);
            isSensorRegistered = false;
        }
        sharedPreferencesHelper.saveStepCount(stepCount);
    }

    private void showTutorialDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Step Tracking Tutorial")
                .setMessage("Welcome! To start counting your running steps please stay in this window and store your phone in your pocket or hold it in your hand.")
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        final float alpha = ALPHA;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linearAcceleration[0] = event.values[0] - gravity[0];
        linearAcceleration[1] = event.values[1] - gravity[1];
        linearAcceleration[2] = event.values[2] - gravity[2];

        float magnitude = (float) Math.sqrt(
                linearAcceleration[0] * linearAcceleration[0] +
                        linearAcceleration[1] * linearAcceleration[1] +
                        linearAcceleration[2] * linearAcceleration[2]
        );

        if (magnitude > STEP_THRESHOLD) {
            stepCount++;
            updateStepCountUI(stepCount);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void updateStepCountUI(int stepCount) {
        stepCountTextView.setText("Step Count: " + stepCount);
    }
}
