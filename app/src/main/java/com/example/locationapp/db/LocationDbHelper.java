package com.example.locationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import android.os.Environment;

public class LocationDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locations.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "location_history";
    private final Context context;

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL, " +
                "timestamp INTEGER NOT NULL)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertLocation(double latitude, double longitude, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("timestamp", timestamp);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1;
    }
    
    /**
     * Exporte la base de données vers le dossier externe accessible (Downloads / Documents)
     */
    public boolean exportDatabaseToDownloads() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);

        // Emplacement de destination : Dossier Downloads accessible à l'utilisateur
        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File exportFile = new File(exportDir, "locations_export.db");

        if (!dbFile.exists()) {
            return false;
        }

        try (InputStream is = new FileInputStream(dbFile);
             OutputStream os = new FileOutputStream(exportFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
