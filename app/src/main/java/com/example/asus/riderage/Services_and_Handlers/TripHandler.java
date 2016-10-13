package com.example.asus.riderage.Services_and_Handlers;

import android.util.Log;

import java.text.DateFormat;

import com.example.asus.riderage.MainActivity;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Database.DataPoint;
import com.example.asus.riderage.Database.TripDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Handles trip while trip is active, includes DB commands for starting & ending the trip
 */

public class TripHandler {
    private static final String TAG = "TripHandler";
    private long tripId;
    private TripDatabaseHelper tripDbHelper;
    private DateFormat dateFormat;
    private Date startDate, endDate;
    private String tripTimeTotal;
    private double averageSpeed, averageRPM;
    private double totalDistance;

    public TripHandler(){
        this.dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        this.tripDbHelper = new TripDatabaseHelper(CommunicationHandler.getCommunicationHandlerInstance().getContext());
    }

    /**
     * Used for storing a datapoint to the DB
     * @param dataPoint
     */
    public void storeDataPointToDB(DataPoint dataPoint){
        //TODO get relevant values from datapoint, store into DBHelper
        this.tripDbHelper.saveDataPoint(dataPoint);

    }

    /**
     * Creates a new row to DB in order to get the trip ID
     */
    public void startNewTrip() {
        this.startDate = new Date();
        this.tripId = this.tripDbHelper.saveTrip(CommunicationHandler.getCommunicationHandlerInstance().getTripName(), null, null, dateFormat.format(this.startDate));
    }


    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    /**
     * Stops the trip, calculates total time for the trip
     */
    public void stopCurrentTrip(){
        Log.e(TAG, "stopCurrentTrip: 1." );
        CommunicationHandler.getCommunicationHandlerInstance().setRunningStatus(false);
        this.endDate = new Date();
        setTripTimeTotal(formatDuration(endDate.getTime() - startDate.getTime()));
    }

    /**
     * Formats MS to HH:MM:SS
     * @param tripTimeTotal time in MS
     * @return formatted time in HH:MM:SS
     */
    private String formatDuration(long tripTimeTotal) {
        String hms = String.format(Locale.getDefault(),"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(tripTimeTotal),
                TimeUnit.MILLISECONDS.toMinutes(tripTimeTotal) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(tripTimeTotal) % TimeUnit.MINUTES.toSeconds(1));
        Log.e(TAG, "formatDuration: time formatted " + hms);
        return hms;
    }

    /**
     * Called when trip ends, saves available trip data to DB
     */
    public void saveTripToDb(){
        Log.e(TAG, "saveTripToDb: 4." );
        this.tripDbHelper.endTrip(this.tripId, MainActivity.round(this.getTotalDistance(), 2), dateFormat.format(this.endDate), getTripTimeTotal(), MainActivity.round(this.getAverageSpeed(), 1), MainActivity.round(this.getAverageRPM(), 0), null, null, null, null);
        CommunicationHandler.getCommunicationHandlerInstance().setTripId(this.tripId);
    }


    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        Log.e(TAG, "setAverageSpeed: ¤avg speed set to# " + averageSpeed );
        this.averageSpeed = averageSpeed;
    }

    public double getAverageRPM() {
        return averageRPM;
    }

    public void setAverageRPM(double averageRPM) {
        Log.e(TAG, "setAverageRPM: #average rpm set to#" + averageRPM );
        this.averageRPM = averageRPM;
    }

    public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance;}

    public double getTotalDistance() {return totalDistance;}

    public String getTripTimeTotal() {return tripTimeTotal;}

    public void setTripTimeTotal(String tripTimeTotal) {
        this.tripTimeTotal = tripTimeTotal;
    }
}
