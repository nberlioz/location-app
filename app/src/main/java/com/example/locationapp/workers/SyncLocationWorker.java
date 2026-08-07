package com.example.locationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.common.util.concurrent.ListenableFuture;
import android.util.Log;

import java.io.File;

public class SyncLocationWorker extends ListenableWorker {

    public SyncLocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    @SuppressLint("MissingPermission")
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                    .addOnSuccessListener(location -> {
                        if (location == null) {
                            completer.set(Result.retry());
                            return;
                        }
                        Log.i("SyncLocationWorker","Location récupéérer avec succes");

                        // Écriture dans SQLite
                        LocationDbHelper dbHelper = new LocationDbHelper(getApplicationContext());
                        boolean inserted = dbHelper.insertLocation(
                                location.getLatitude(),
                                location.getLongitude(),
                                location.getTime()
                        );
                        if(inserted) {
                          Log.i("SyncLocationWorker","données inséré avec succes");
                          dbHelper.exportDatabaseToDownloads();
                        }
                        dbHelper.close();

                        if (inserted) {
                            completer.set(Result.success());
                        } else {
                            completer.set(Result.retry());
                        }
                    })
                    .addOnFailureListener(exception -> {
                        exception.printStackTrace();
                        completer.set(Result.failure());
                    });

            return "SyncLocationWorkerJob";
        });
    }
}
