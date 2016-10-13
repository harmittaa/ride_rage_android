package com.example.asus.riderage.Misc;


import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Async Task for initialization of result view
 * draws <code>PolyLineOptions</code> and populates the <code>TextViews</code>
 */
public class TripDataParser extends AsyncTask<Integer, Long, Boolean> {
    private final String TAG = this.getClass().getName();
    private TripDatabaseHelper dbHelper;

    public TripDataParser() {}

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * doInBackground start that calls DbHelper for cursors and proceeds to call needed methods
     * @param params Parameters for the doInBackground
     * @return Returns null
     */
    @Override
    protected Boolean doInBackground(Integer... params) {
        Log.e(TAG, "doInBackground: 7.");
        this.dbHelper = new TripDatabaseHelper(CommunicationHandler.getCommunicationHandlerInstance().getContext());
        final Cursor tripDataCursor = this.dbHelper.getFullTripData(CommunicationHandler.getCommunicationHandlerInstance().getTripId());
        final Cursor dataPointCursor = this.dbHelper.getDataPoints(CommunicationHandler.getCommunicationHandlerInstance().getTripId());
        dataPointCursor.moveToFirst();
        tripDataCursor.moveToFirst();
        calculateAverages(dataPointCursor, tripDataCursor);
        return null;
    }

    /**
     * Checks if the average speed and RPM are directly available
     * if not then calculates them based on DataPoint cursor.
     * <p>Calls {@link #calculateDuration(Cursor)} and {@link #getDistanceFromCursor(Cursor)} if duration / distance is not directly available.</p>
     *
     * @param dataPointCursor Cursor holding DataPoints
     * @param tripDataCursor  Cursor holding the Trip data
     */
    private void calculateAverages(Cursor dataPointCursor, Cursor tripDataCursor) {
        double avgSpeed = 0.0;
        double avgRpm = 0.0;
        int counter = 0;
        dataPointCursor.moveToFirst();
        tripDataCursor.moveToFirst();
        String avgrpm;
        String avgspd;
        try {
            avgrpm = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_AVERAGE_RPM));
            avgspd = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_AVERAGE_SPEED));
            if (TextUtils.isEmpty(avgrpm) || TextUtils.isEmpty(avgspd)) {
                while (dataPointCursor.moveToNext()) {
                    counter++;
                    avgRpm += Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM)));
                    avgSpeed += Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_SPEED)));
                }
                if (avgRpm != 0) avgRpm = avgRpm / counter;
                if (avgSpeed != 0) avgSpeed = avgSpeed / counter;

            } else {
                avgRpm = Double.parseDouble(avgrpm);
                avgSpeed = Double.parseDouble(avgspd);
            }
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e(TAG, "calculateAverages: NO DATA POINTS", e);
        }

        String duration = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_DURATION));
        String distance = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_DISTANCE));
        if (duration == null || distance == null) {
            duration = calculateDuration(dataPointCursor);
            distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", getDistanceFromCursor(dataPointCursor))) + "KM";
        }

        dbHelper.editTripValues(CommunicationHandler.getCommunicationHandlerInstance().getTripId(), avgSpeed, avgRpm, duration, distance);
    }

    /**
     * Calculates distance travelled based on coordinates from Cursor
     *
     * @param dataPointCursor <p>Cursor that holds DataPoints for the Trip</p>
     */
    private double getDistanceFromCursor(Cursor dataPointCursor) {
        double totalDistance = 0.0;
        Location location = new Location("ResultFragment");
        Location previousLocation = new Location("Make");
        dataPointCursor.moveToFirst();

        while (dataPointCursor.moveToNext()) {
            location.setLatitude(dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE)));
            location.setLongitude(dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)));
            if (previousLocation.getLatitude() != 0.0) {
                totalDistance += location.distanceTo(previousLocation) / 1000;
            }

            previousLocation.setLatitude(location.getLatitude());
            previousLocation.setLongitude(location.getLongitude());
        }
        return totalDistance;
    }

    /**
     * Calculates the time between the first and the last DataPoints in Cursor.
     *
     * @param dataPointCursor <p>Cursor holding DataPoints</p>
     */
    private String calculateDuration(Cursor dataPointCursor) {
        String duration = "";
        dataPointCursor.moveToFirst();
        String startDate = dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_TIMESTAMP));
        dataPointCursor.moveToLast();
        String endDate = dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_TIMESTAMP));
        Log.e(TAG, "calculateDuration: enddate " + endDate + " start " + startDate);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        try {
            Date startDateFormatted = dateFormat.parse(startDate);
            Date endDateFormatted = dateFormat.parse(endDate);
            duration = formatDuration(endDateFormatted.getTime() - startDateFormatted.getTime());
        } catch (ParseException e) {
            Log.e(TAG, "calculateAverages error ", e);
        }
        return duration;
    }

    /**
     * Formats milliseconds to HH:MM:SS
     *
     * @param tripTimeTotal Milliseconds to the formatted
     */
    private String formatDuration(long tripTimeTotal) {
        String hms = String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(tripTimeTotal),
                TimeUnit.MILLISECONDS.toMinutes(tripTimeTotal) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(tripTimeTotal) % TimeUnit.MINUTES.toSeconds(1));
        Log.e(TAG, "formatDuration: time formatted " + hms);
        return hms;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}
