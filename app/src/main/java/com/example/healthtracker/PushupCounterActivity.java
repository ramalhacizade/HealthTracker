package com.example.healthtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;

public class PushupCounterActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private int pushupCount = 0;
    private boolean proximityNear = false;

    private TextView pushupCountTextView;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pushup_counter);

        pushupCountTextView = findViewById(R.id.pushupCountTextView);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        showPushupGuideDialog();
    }
    private void showPushupGuideDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);

        ImageView dialogImage = dialogView.findViewById(R.id.dialog_image);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);

        new AlertDialog.Builder(this)
                .setTitle("Push-Up Guide")
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        pushupCount = sharedPreferencesHelper.getPushupCount();
        updatePushupCount();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        sharedPreferencesHelper.savePushupCount(pushupCount);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            float sensitivityThreshold = 0.1f;
            if (distance < sensitivityThreshold) {
                if (!proximityNear) {
                    proximityNear = true;
                    pushupCount++;
                    updatePushupCount();
                }
            } else {
                proximityNear = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void updatePushupCount() {
        pushupCountTextView.setText("Push-Up Count: " + pushupCount);
    }
}
