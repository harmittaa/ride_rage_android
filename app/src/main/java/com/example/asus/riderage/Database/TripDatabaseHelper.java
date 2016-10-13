package com.example.asus.riderage.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.example.asus.riderage.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * SQLiteOpenHelper class, includes DB methods and CRUD methods
 */

public class TripDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "TripDatabseHelper";
    private SQLiteDatabase database;
    private Cursor cursor;
    private static final String DB_NAME = "TripDatabase";
    private static final int DB_VERSION = 1;

    private static final String TABLE_TRIP = "trip";
    private static final String TRIP_ID = "trip_id";
    public static final String TRIP_TITLE = "title";
    private static final String TRIP_VEHICLE_ID = "trip_vehicle_id";
    public static final String TRIP_DISTANCE = "distance";
    public static final String TRIP_DURATION = "duration";
    private static final String TRIP_START_TIME = "start_time";
    private static final String TRIP_END_TIME = "end_time";
    public static final String TRIP_AVERAGE_SPEED = "average_speed";
    public static final String TRIP_AVERAGE_RPM = "average_rpm";
    private static final String TRIP_AVERAGE_CONSUMPTION = "average_consumption";
    private static final String TRIP_TOTAL_CONSUMPTION = "total_consumption";
    private static final String TRIP_GAS_COST = "gas_cost";
    private static final String TRIP_DRIVING_ANALYSIS = "driving_analysis";

    private static final String SQL_CREATE_TRIP_TABLE =
            "CREATE TABLE " + TABLE_TRIP + " (" +
                    TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TRIP_TITLE + " text not null, " +
                    TRIP_VEHICLE_ID + " integer, " +
                    TRIP_DISTANCE + " real, " +
                    TRIP_START_TIME + " DATETIME not null, " +
                    TRIP_END_TIME + " DATETIME, " +
                    TRIP_DURATION + " text, " +
                    TRIP_AVERAGE_SPEED + " real, " +
                    TRIP_AVERAGE_RPM + " real, " +
                    TRIP_AVERAGE_CONSUMPTION + " real, " +
                    TRIP_TOTAL_CONSUMPTION + " real, " +
                    TRIP_GAS_COST + " real, " +
                    TRIP_DRIVING_ANALYSIS + " text);";

    private static final String TABLE_DATAPOINT = "datapoint";
    private static final String DATAPOINT_ID = "datapoint_id";
    private static final String DATAPOINT_TRIP_ID = "datapoint_trip_id";
    public static final String DATAPOINT_SPEED = "datapoint_speed";
    public static final String DATAPOINT_RPM = "datapoint_rpm";
    private static final String DATAPOINT_ACCELERATION = "datapoint_acceleration";
    private static final String DATAPOINT_CONSUMPTION = "datapoint_consumption";
    public static final String DATAPOINT_LONGITUDE = "datapoint_longitude";
    public static final String DATAPOINT_LATITUDE = "datapoint_latitude";
    public static final String DATAPOINT_TIMESTAMP = "datapoint_timestamp";


    private static final String SQL_CREATE_DATAPOINT_TABLE =
            "CREATE TABLE " + TABLE_DATAPOINT + " (" +
                    DATAPOINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATAPOINT_SPEED + " real not null, " +
                    DATAPOINT_RPM + " real not null, " +
                    DATAPOINT_ACCELERATION + " real not null, " +
                    DATAPOINT_LONGITUDE + " real not null, " +
                    DATAPOINT_LATITUDE + " real not null, " +
                    DATAPOINT_TIMESTAMP + " string not null, " +
                    DATAPOINT_CONSUMPTION + " real not null, " +
                    DATAPOINT_TRIP_ID + " integer, " +
                    " FOREIGN KEY (" + DATAPOINT_TRIP_ID + ") REFERENCES " + TABLE_TRIP + "(" + TRIP_ID + "));";


    private static final String TABLE_VEHICLE = "vehicle";
    private static final String VEHICLE_ID = "vehicle_id";
    private static final String VEHICLE_NAME = "vehicle";
    private static final String SQL_CREATE_VEHICLE_TABLE =
            "CREATE TABLE " + TABLE_VEHICLE + " (" +
                    VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    VEHICLE_NAME + " text not null);";

    public TripDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.database = this.getWritableDatabase();
    }

    /**
     * Default onCreate for the DB.
     * @param db The Database instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRIP_TABLE);
        db.execSQL(SQL_CREATE_VEHICLE_TABLE);
        db.execSQL(SQL_CREATE_DATAPOINT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trip");
        db.execSQL("DROP TABLE IF EXISTS vehicle");
        db.execSQL("DROP TABLE IF EXISTS datapoint");
    }

    /**
     * Saves trip start parameters to DB
     *
     * @param title      Trip title
     * @param vehicleId  The vehicle ID
     * @param distance   total distance
     * @param start_time Start time
     * @return returns the row ID of the new trip
     */
    public long saveTrip(String title, Integer vehicleId, Double distance, String start_time) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_TITLE, title);
        values.put(TRIP_VEHICLE_ID, vehicleId);
        values.put(TRIP_DISTANCE, distance);
        values.put(TRIP_START_TIME, start_time);

        long newRowId = this.database.insert(TABLE_TRIP, null, values);
        values.clear();
        this.database.close();
        return newRowId;
    }


    /**
     * Called when user clicks on a trip from the trip list
     *
     * @param id ID for the trip of which data is going to be fetched
     * @return returns the cursor which holds the rows of the query
     */
    public Cursor getFullTripData(long id) {
        this.database = this.getReadableDatabase();
        // define SELECT fields
        String[] projection = {
                TRIP_ID + " as " + "_id",
                TRIP_TITLE,
                TRIP_VEHICLE_ID,
                TRIP_DISTANCE,
                TRIP_START_TIME,
                TRIP_END_TIME,
                TRIP_DURATION,
                TRIP_AVERAGE_SPEED,
                TRIP_AVERAGE_RPM,
                TRIP_AVERAGE_CONSUMPTION,
                TRIP_GAS_COST,
                TRIP_DRIVING_ANALYSIS
        };

        // define the query, searches from TABLE_TRIP with the defined projection
        // and where the ID is like the id provided
        this.cursor = this.database.query(
                TABLE_TRIP,
                projection,
                "trip_id like " + id,
                null,
                null,
                null,
                null,
                null
        );
        Log.e(TAG, "getFullTripData: cursor size " + cursor.getCount());
        return this.cursor;
    }

    /**
     * Called for populating the list view of all the trips
     *
     * @return Returns the cursor which holds the Trip headers
     */
    public Cursor getTripHeaders() {
        this.database = this.getReadableDatabase();

        this.cursor = this.database.rawQuery("SELECT " + TRIP_ID + " as " + "_id, " + TRIP_TITLE + ", " + TRIP_START_TIME + ", " + TRIP_DISTANCE + ", " + TRIP_DURATION +
                " FROM " + TABLE_TRIP + " ORDER BY " + TRIP_ID + " DESC;", null, null);
        return this.cursor;
    }

    public void saveDataPoint(DataPoint dataPoint) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATAPOINT_TRIP_ID, dataPoint.getTripId());
        values.put(DATAPOINT_RPM, dataPoint.getRpm());
        values.put(DATAPOINT_SPEED, dataPoint.getSpeed());
        values.put(DATAPOINT_ACCELERATION, dataPoint.getAcceleration());
        values.put(DATAPOINT_CONSUMPTION, dataPoint.getConsumption());
        values.put(DATAPOINT_LONGITUDE, dataPoint.getLongitude());
        values.put(DATAPOINT_LATITUDE, dataPoint.getLatitude());
        values.put(DATAPOINT_TIMESTAMP, dataPoint.getTimestamp());
        this.database.insert(TABLE_DATAPOINT, null, values);
        values.clear();
        this.database.close();
    }


    public long saveVehicle(String vehicleName) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VEHICLE_NAME, vehicleName);
        long vehicleId = this.database.insert(TABLE_VEHICLE, null, values);
        values.clear();
        this.database.close();
        return vehicleId;
    }


    public Cursor getVehicles() {
        this.database = this.getReadableDatabase();
        String[] projection = {
                VEHICLE_ID + " as " + "_id",
                VEHICLE_NAME
        };

        // creates the query ordered by ID's in descending order
        this.cursor = this.database.query(
                TABLE_VEHICLE,
                projection,
                null,
                null,
                null,
                null,
                null
        );
        return this.cursor;
    }

    /**
     * Adds data at the end on the trip to DB.
     */
    public void endTrip(long tripId, Double distance, String end_time, String duration, Double averageSpeed, Double averageRPM, Double averageConsumption, Double totalConsumption,
                        Double gasCost, String analysis) {
        Log.e(TAG, "endTrip params:\ntripid " + tripId + "\ndistance " + end_time + "\nduration " + duration + "\naveragespeed " + averageSpeed + "\naveragerpm " + averageRPM + "\nconsumption " + totalConsumption);
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_END_TIME, end_time);
        values.put(TRIP_DISTANCE, distance);
        values.put(TRIP_END_TIME, end_time);
        values.put(TRIP_DURATION, duration);
        values.put(TRIP_AVERAGE_SPEED, averageSpeed);
        values.put(TRIP_AVERAGE_RPM, averageRPM);
        values.put(TRIP_AVERAGE_CONSUMPTION, averageConsumption);
        values.put(TRIP_TOTAL_CONSUMPTION, totalConsumption);
        values.put(TRIP_GAS_COST, "2");
        values.put(TRIP_DRIVING_ANALYSIS, "Example analysis");
        this.database.update(TABLE_TRIP, values, TRIP_ID + "=" + tripId, null);
    }

    /**
     * Gets datapoints for a trip.
     *
     * @param tripId Defines the trip of which DataPoints to fetch
     * @return returns cursor that holds the DataPoints
     */
    public Cursor getDataPoints(long tripId) {
        this.database = this.getReadableDatabase();
        this.cursor = this.database.rawQuery("SELECT " + DATAPOINT_ID + ", " + DATAPOINT_RPM + ", " + DATAPOINT_SPEED + ", " + DATAPOINT_LATITUDE + ", " + DATAPOINT_LONGITUDE + ", " + DATAPOINT_TIMESTAMP +
                " FROM " + TABLE_DATAPOINT +
                " WHERE " + DATAPOINT_TRIP_ID + " = " + tripId, null, null);
        return this.cursor;

    }

    /**
     * <p>Edits title of a trip with a spesific ID</p>
     * @param newName <p>New name to be given to the trip</p>
     * @param tripId <p>Id of the trip to be edited</p>
     */
    public void editTripName(String newName, long tripId){
        this.database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRIP_TITLE, newName);

        this.database.update(TABLE_TRIP,values,TRIP_ID + " = " + tripId,null);
    }

    /**
     * Updates trip's values in case TripHandler has failed
     * @param tripId ID for the trip that's going to be updated
     * @param averageSpeed Average speed of the trip
     * @param averageRpm Average RPM of the trip
     * @param duration Total duration in HH:MM:SS of the trip
     * @param distance Total distance of the trip
     */
    public void editTripValues(long tripId, double averageSpeed, double averageRpm, String duration, String distance) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_AVERAGE_SPEED, averageSpeed);
        values.put(TRIP_AVERAGE_RPM, averageRpm);
        values.put(TRIP_DURATION, duration);
        values.put(TRIP_DISTANCE, distance);
        this.database.update(TABLE_TRIP, values, TRIP_ID + " = " + tripId, null);
    }

    /**
     * Deletes the Trip and associated datapoints from the DB
     *
     * @param tripId The ID of the trip that needs to be deleted
     * @return Returns true when one or more rows from TABLE_TRIP are deleted
     */
    public boolean deleteTrip(long tripId) {
        this.database = this.getWritableDatabase();
        this.database.delete(TABLE_DATAPOINT, DATAPOINT_TRIP_ID + " = " + tripId, null);
        return this.database.delete(TABLE_TRIP, TRIP_ID + " = " + tripId, null) > 0;
    }


    /**
     * Calculates the total distance driven based on the TRIP_DISTANCE field in TABLE_TRIP.
     * @return Total distance driven in kilometers rounded to two decimals using {@link MainActivity#round(double, int)}.
     */
    public double getTotalDistanceDriven() {
        this.database = this.getReadableDatabase();
        // define SELECT fields
        String[] projection = {
                TRIP_DISTANCE
        };

        // define the query, searches from TABLE_TRIP with the defined projection
        this.cursor = this.database.query(
                TABLE_TRIP,
                projection,
                null,
                null,
                null,
                null,
                null,
                null
        );

        this.cursor.moveToFirst();
        double totalDistance = 0.0;
        if (this.cursor.getCount() > 0) {
            while (this.cursor.moveToNext()) {
                totalDistance += this.cursor.getDouble(this.cursor.getColumnIndexOrThrow(TRIP_DISTANCE));
            }
        }
        return MainActivity.round(totalDistance, 2);


    };

    /**
     * Function for exporting the database, creates a folder in downloads "BackupFolder" and saves
     * the database file there.
     */
    public void exportDatabase() {
        //creating a new folder for the database to be backuped to
        File direct = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/BackupFolder");

        Log.e(TAG, "exportDatabase: dir already exists?" +  direct.exists());
        if(!direct.exists())
        {
            if(direct.mkdir()){
                Log.e(TAG, "exportDatabase: dir exists?" +  direct.exists());
            } else {
                Log.e(TAG, "exportDatabase: failed to create directory? " + direct.mkdir() );
            }

        }
        Log.e(TAG, "exportDatabase: " + direct.getAbsolutePath());
        doExport();

    }

    /**
     * Handles the exporting of the database file
     */
    private void doExport() {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {

                String  currentDBPath= "/data/com.example.asus.riderage/databases/TripDatabase";
                String backupDBPath  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/BackupFolder/test";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(backupDBPath);

                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.e(TAG, "exportDatabase: success" );
            } else {
                Log.e(TAG, "doExport: canwrite " +  sd.canWrite() );
            }
        } catch (Exception e) {
            Log.e(TAG, "exportDatabase: failed", e );
        }
    }
}