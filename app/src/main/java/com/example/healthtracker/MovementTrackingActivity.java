package com.example.healthtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MovementTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String MOVEMENT_PREFS_NAME = "MovementTrackingPrefs";
    private static final String KEY_TOTAL_DISTANCE = "totalDistance";

    private SharedPreferencesHelper sharedPreferencesHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private float totalDistance = 0;
    private boolean tracking = false;

    private TextView distanceTextView;
    private Button startButton;
    private Button stopButton;

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movement_tracking);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        distanceTextView = findViewById(R.id.distanceTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        totalDistance = prefs.getFloat(Constants.KEY_TOTAL_DISTANCE, 0);
        updateUI();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (lastLocation != null && tracking) {
                        totalDistance += lastLocation.distanceTo(location);
                        saveTotalDistance(totalDistance);
                    }
                    lastLocation = location;
                    updateUI();
                    updateMapLocation(location);
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startLocationUpdates();
        }

        startButton.setOnClickListener(v -> startTracking());

        stopButton.setOnClickListener(v -> stopTracking());
    }

    private void showTutorialDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Movement Tracking Tutorial")
                .setMessage("Welcome! To start tracking your movement on the map, press START. When you are done, press STOP. Please keep your GPS open while moving.")
                .setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void saveTotalDistance(float distance) {
        SharedPreferences.Editor editor = getSharedPreferences(MOVEMENT_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(KEY_TOTAL_DISTANCE, distance);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void updateUI() {
        String formattedDistance = String.format("%.1f", totalDistance);
        distanceTextView.setText("Distance: " + formattedDistance + " meters");
    }

    private void updateMapLocation(Location location) {
        if (googleMap != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Position"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }
    }

    private void startTracking() {
        tracking = true;
        totalDistance = 0;
        lastLocation = null;
        Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show();
    }

    private void stopTracking() {
        tracking = false;
        Toast.makeText(this, "Tracking stopped. Total distance: " + totalDistance + " meters", Toast.LENGTH_SHORT).show();
        saveTotalDistance(totalDistance);
        sharedPreferencesHelper.saveMovementDistance(this, totalDistance);
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
        showTutorialDialog();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}