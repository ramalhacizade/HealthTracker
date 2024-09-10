package com.example.healthtracker;

import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_SETTINGS = 1001;

    private SharedPreferencesHelper sharedPreferencesHelper;
    private TextView summaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        summaryTextView = findViewById(R.id.summaryTextView);

        String savedDate = sharedPreferencesHelper.getDate(this); // Pass context here
        String currentDate = sharedPreferencesHelper.getCurrentDate();
        if (!savedDate.equals(currentDate)) {
            sharedPreferencesHelper.resetDataForNewDay(this); // Pass context here
            sharedPreferencesHelper.saveDate(currentDate, this); // Pass context here
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button buttonStepTracker = findViewById(R.id.buttonStepTracker);
        buttonStepTracker.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, StepTrackerActivity.class)));

        Button buttonMovementTracking = findViewById(R.id.buttonMovementTracking);
        buttonMovementTracking.setOnClickListener(v -> {
            if (!isLocationEnabled()) {
                showLocationSettings();
            } else {
                startActivity(new Intent(MainActivity.this, MovementTrackingActivity.class));
            }
        });

        Button pushupCounterButton = findViewById(R.id.buttonPushupCounter);
        pushupCounterButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PushupCounterActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayDailySummary();
    }

    private void displayDailySummary() {
        int stepCount = sharedPreferencesHelper.getStepCount(this);
        float movementDistance = sharedPreferencesHelper.getMovementDistance();
        int pushupCount = sharedPreferencesHelper.getPushupCount();


        String formattedDistance = String.format("%.1f", movementDistance);
        String summary = "Today's Summary:\n"
                + "Step Count: " + stepCount + "\n"
                + "Movement Distance: " + formattedDistance + " meters\n"
                + "Push-up Count: " + pushupCount;

        summaryTextView.setText(summary);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showLocationSettings() {
        Toast.makeText(this, "Please enable GPS to use this feature", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, REQUEST_LOCATION_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_SETTINGS) {
            if (isLocationEnabled()) {
                startActivity(new Intent(MainActivity.this, MovementTrackingActivity.class));
            } else {
                Toast.makeText(this, "GPS is still not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
