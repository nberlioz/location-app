package com.example.locationapp;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class LocationScheduler {

    private static final String UNIQUE_WORK_NAME = "PeriodicLocationSyncWork";

    public static void scheduleLocationSync(Context context) {
        // 1. Définition des contraintes d'exécution
        Constraints constraints = new Constraints.Builder()
                // N'exécute la tâche que si la batterie n'est pas faible
                .setRequiresBatteryNotLow(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        // 2. Création de la requête périodique (15 minutes = minimum système Android)
        PeriodicWorkRequest locationWorkRequest =
                new PeriodicWorkRequest.Builder(
                        SyncLocationWorker.class,
                        15, TimeUnit.MINUTES, // Intervalle de répétition
                        5, TimeUnit.MINUTES)  // Fenêtre de flexibilité
                        .setConstraints(constraints)
                        .build();

        // 3. Enregistrement auprès du WorkManager
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                // KEEP : conserve la tâche existante si elle est déjà planifiée, évite les doublons
                ExistingPeriodicWorkPolicy.KEEP, 
                locationWorkRequest
        );
    }

    // Méthode pour annuler la synchronisation si besoin
    public static void cancelLocationSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME);
    }
}
