package com.example.asus.riderage.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Asus on 29/09/2016.
 */

// Class used for saving trip data into SQL database
public class TripDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "TripDatabseHelper";
    private SQLiteDatabase database;
    private Cursor cursor;

    private static final String DB_NAME = "TripDatabase";
    private static final int DB_VERSION = 1;

    private static final String TABLE_TRIP = "trip";
    private static final String TRIP_ID = "trip_id";
    public static final String TRIP_TITLE = "title";
    public static final String TRIP_VEHICLE_ID = "trip_vehicle_id";
    public static final String TRIP_DISTANCE = "distance";
    public static final String TRIP_DURATION_MS = "duration";
    public static final String TRIP_START_TIME = "start_time";
    public static final String TRIP_END_TIME = "end_time";
    public static final String TRIP_AVERAGE_SPEED = "average_speed";
    public static final String TRIP_AVERAGE_RPM = "average_rpm";
    public static final String TRIP_AVERAGE_CONSUMPTION = "average_consumption";
    public static final String TRIP_TOTAL_CONSUMPTION = "total_consumption";
    public static final String TRIP_GAS_COST = "gas_cost";
    public static final String TRIP_DRIVING_ANALYSIS = "driving_analysis";

    private static final String SQL_CREATE_TRIP_TABLE =
            "CREATE TABLE " + TABLE_TRIP + " (" +
                    TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TRIP_TITLE + " text not null, " +
                    TRIP_VEHICLE_ID + " integer, " +
                    TRIP_DISTANCE + " real, " +
                    TRIP_START_TIME + " DATETIME not null, " +
                    TRIP_END_TIME + " DATETIME, " +
                    TRIP_DURATION_MS + " integer, " +
                    TRIP_AVERAGE_SPEED + " real, " +
                    TRIP_AVERAGE_RPM + " real, " +
                    TRIP_AVERAGE_CONSUMPTION + " real, " +
                    TRIP_TOTAL_CONSUMPTION + " real, " +
                    TRIP_GAS_COST + " real, " +
                    TRIP_DRIVING_ANALYSIS + " text);";

    public static final String TABLE_DATAPOINT = "datapoint";
    public static final String DATAPOINT_ID = "datapoint_id";
    public static final String DATAPOINT_TRIP_ID = "datapoint_trip_id";
    public static final String DATAPOINT_SPEED = "datapoint_speed";
    public static final String DATAPOINT_RPM = "datapoint_rpm";
    public static final String DATAPOINT_ACCELERATION = "datapoint_acceleration";
    public static final String DATAPOINT_CONSUMPTION = "datapoint_consumption";
    public static final String DATAPOINT_LONGITUDE = "datapoint_longitude";
    public static final String DATAPOINT_LATITUDE = "datapoint_latitude";


    private static final String SQL_CREATE_DATAPOINT_TABLE =
            "CREATE TABLE " + TABLE_DATAPOINT + " (" +
                    DATAPOINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    DATAPOINT_TRIP_ID + " integer not null, " +
                    DATAPOINT_SPEED + " real not null, " +
                    DATAPOINT_RPM + " real not null, " +
                    DATAPOINT_ACCELERATION + " real not null, " +
                    DATAPOINT_LONGITUDE + " real not null, " +
                    DATAPOINT_LATITUDE + " real not null, " +
                    DATAPOINT_CONSUMPTION + " real not null);";


    private static final String TABLE_VEHICLE = "vehicle";
    private static final String VEHICLE_ID = "vehicle_id";
    public static final String VEHICLE_NAME = "vehicle";
    private static final String SQL_CREATE_VEHICLE_TABLE =
            "CREATE TABLE " + TABLE_VEHICLE + " (" +
                    VEHICLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    VEHICLE_NAME + " text not null);";

    public TripDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // init the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TRIP_TABLE);
        db.execSQL(SQL_CREATE_VEHICLE_TABLE);
        db.execSQL(SQL_CREATE_DATAPOINT_TABLE);
        Log.e(TAG, "SQLite DB created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS trip");
        db.execSQL("DROP TABLE IF EXISTS vehicle");
        db.execSQL("DROP TABLE IF EXISTS datapoint");
    }

    // once trip is finished save all the data to the DB
    public long saveTrip(String title, Integer vehicleId, Double distance, String start_time, String end_time,
                         Long durationMs, Double averageSpeed, Double averageRPM, Double averageConsumption, Double totalConsumption,
                         Double gasCost, String analysis) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_TITLE, title);
        values.put(TRIP_VEHICLE_ID, vehicleId);
        values.put(TRIP_DISTANCE, distance);
        values.put(TRIP_START_TIME, start_time);/*
        if(end_time != null) values.put(TRIP_END_TIME, end_time.toString());
        values.put(TRIP_DURATION_MS, durationMs);
        values.put(TRIP_AVERAGE_SPEED, averageSpeed);
        values.put(TRIP_AVERAGE_RPM, averageRPM);
        values.put(TRIP_AVERAGE_CONSUMPTION, averageConsumption);
        values.put(TRIP_TOTAL_CONSUMPTION, totalConsumption);
        values.put(TRIP_GAS_COST, gasCost);
        values.put(TRIP_DRIVING_ANALYSIS, analysis);*/

        long newRowId = this.database.insert(TABLE_TRIP, null, values);
        Log.e(TAG, "New trip saved for ID " + newRowId);
        values.clear();
        this.database.close();
        return newRowId;
    }

    // called when user clicks on a trip from the trip list
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
                TRIP_DURATION_MS,
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
        Log.e(TAG, "getFullTripData: data fetched");
        return this.cursor;
    }

    //called for populating the list view of all the trips
    public Cursor getTripHeaders() {
        this.database = this.getReadableDatabase();
        String[] projection = {
                TRIP_ID + " as " + "_id",
                TRIP_TITLE,
                TRIP_START_TIME,
                TRIP_DISTANCE,
                TRIP_VEHICLE_ID
        };

        // raw query with join to get vehicle name from the VEHICLE table
        this.cursor = this.database.rawQuery("SELECT " + TRIP_ID + ", " + TRIP_TITLE + ", " + TRIP_START_TIME + ", " + TRIP_DISTANCE + ", " + TRIP_VEHICLE_ID + ", " + VEHICLE_NAME +
                " FROM " + TABLE_TRIP +
                " JOIN " + TABLE_VEHICLE +
                " ON " + TRIP_VEHICLE_ID + " = " + VEHICLE_ID, null, null);
        return this.cursor;
    }

    public void saveDataPoint(DataPoint dataPoint) {
        Log.e(TAG, "saveDataPoint: ");
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATAPOINT_TRIP_ID, dataPoint.getTripId());
        values.put(DATAPOINT_RPM, dataPoint.getRpm());
        values.put(DATAPOINT_SPEED, dataPoint.getSpeed());
        values.put(DATAPOINT_ACCELERATION, dataPoint.getAcceleration());
        values.put(DATAPOINT_CONSUMPTION, dataPoint.getConsumption());
        values.put(DATAPOINT_LONGITUDE, dataPoint.getLongitude());
        values.put(DATAPOINT_LATITUDE, dataPoint.getLatitude());
        long datapointId = this.database.insert(TABLE_DATAPOINT, null, values);
        Log.e(TAG, "Datapoint saved id " + datapointId);
        values.clear();
        this.database.close();
    }


    public long saveVehicle(String vehicleName) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(VEHICLE_NAME, vehicleName);
        long vehicleId = this.database.insert(TABLE_VEHICLE, null, values);
        Log.e(TAG, "Vehicle saved");
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

    public void endTrip(long tripId, Double distance, String end_time, Long durationMs, Double averageSpeed, Double averageRPM, Double averageConsumption, Double totalConsumption,
                        Double gasCost, String analysis) {
        this.database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRIP_END_TIME, end_time);
        values.put(TRIP_DISTANCE, "100");
        values.put(TRIP_END_TIME, end_time);
        values.put(TRIP_DURATION_MS, durationMs);
        values.put(TRIP_AVERAGE_SPEED, averageSpeed);
        values.put(TRIP_AVERAGE_RPM, averageRPM);
        values.put(TRIP_AVERAGE_CONSUMPTION, averageConsumption);
        values.put(TRIP_TOTAL_CONSUMPTION, totalConsumption);
        values.put(TRIP_GAS_COST, "2");
        values.put(TRIP_DRIVING_ANALYSIS, "Example analysis");
        this.database.update(TABLE_TRIP, values, TRIP_ID + "="+tripId, null);
        Log.e(TAG, "endTrip: Database Updated");
    }

    public Cursor getDataPoints(long tripId) {
        this.database = this.getReadableDatabase();
        this.cursor = this.database.rawQuery("SELECT " + DATAPOINT_ID + ", " + DATAPOINT_RPM + ", " + DATAPOINT_LATITUDE + ", " + DATAPOINT_LONGITUDE +
                " FROM " + TABLE_DATAPOINT +
                " WHERE " + DATAPOINT_TRIP_ID + " = " + tripId, null, null);
        Log.e(TAG, "getDataPoints: datapoints fetched get count" + cursor.getCount() );
        return this.cursor;

    }
}