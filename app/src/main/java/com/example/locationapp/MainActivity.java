package com.example.locationapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE_LOCATION = 1001;
    private static final int PERMISSION_REQUEST_CODE_BACKGROUND = 1002;

    private TextView tvStatus;
    private Button btnToggle;
    private boolean isTrackingActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.tvStatus);
        btnToggle = findViewById(R.id.btnToggle);

        btnToggle.setOnClickListener(v -> {
            if (isTrackingActive) {
                stopLocationTracking();
            } else {
                checkAndRequestPermissions();
            }
        });
    }

    // 1. Vérification et demande des permissions de premier plan
    private void checkAndRequestPermissions() {
        boolean fineLocationGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocationGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineLocationGranted && coarseLocationGranted) {
            // Permissions de premier plan accordées -> Vérification de la permission en arrière-plan
            checkBackgroundLocationPermission();
        } else {
            // Demande des permissions de premier plan
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    PERMISSION_REQUEST_CODE_LOCATION
            );
        }
    }

    // 2. Demande explicite de la permission en arrière-plan (Android 10+ / API 29+)
    private void checkBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            boolean backgroundGranted = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if (backgroundGranted) {
                startLocationTracking();
            } else {
                // Demande séparée imposée par Android pour la position en arrière-plan
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        PERMISSION_REQUEST_CODE_BACKGROUND
                );
            }
        } else {
            // Android 9 et inférieur : pas de permission spécifique requise pour le background
            startLocationTracking();
        }
    }

    // 3. Gestion des réponses aux demandes de permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions de premier plan acceptées -> Vérifier le background
                checkBackgroundLocationPermission();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_CODE_BACKGROUND) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking();
            } else {
                Toast.makeText(this, "La permission en arrière-plan est nécessaire pour enregistrer la position écran éteint.", Toast.LENGTH_LONG).show();
                // On peut quand même démarrer le suivi si souhaité, mais il sera limité au premier plan
                startLocationTracking();
            }
        }
    }

    // 4. Démarrage de la planification
    private void startLocationTracking() {
        LocationScheduler.scheduleLocationSync(this);
        isTrackingActive = true;
        tvStatus.setText("Service de géolocalisation actif (Toutes les 15 min)");
        btnToggle.setText(R.string.stop_service);
        Toast.makeText(this, "Suivi démarré", Toast.LENGTH_SHORT).show();
    }

    // 5. Arrêt de la planification
    private void stopLocationTracking() {
        LocationScheduler.cancelLocationSync(this);
        isTrackingActive = false;
        tvStatus.setText("Service de géolocalisation inactif");
        btnToggle.setText(R.string.start_service);
        Toast.makeText(this, "Suivi arrêté", Toast.LENGTH_SHORT).show();
    }
}