package com.example.asus.riderage.Services_and_Handlers;

import android.content.Intent;
import android.util.Log;

import java.text.DateFormat;

import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Database.DataPoint;
import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.Database.TripObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 30/09/2016.
 */

public class TripHandler {
    private static final String TAG = "TripHandler";
    private TripObject currentTrip;
    private long tripId;
    private TripDatabaseHelper tripDbHelper;
    private DateFormat dateFormat;
    private Date startDate, endDate;
    private String tripTimeTotal;
    private double averageSpeed, averageRPM;
    private double totalDistance;

    public TripHandler(){
        this.dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        this.tripDbHelper = new TripDatabaseHelper(CommunicationHandler.getCommunicationHandlerInstance().getContext());
    }

    public void storeDataPointToDB(DataPoint dataPoint){
        //TODO get relevant values from datapoint, store into DBHelper
        this.tripDbHelper.saveDataPoint(dataPoint);

    }

    public TripObject getCurrentTrip() {
        return currentTrip;
    }

    public void setCurrentTrip(TripObject currentTrip) {
        this.currentTrip = currentTrip;
    }

    public void startNewTrip() {
        this.startDate = new Date();
        this.tripId = this.tripDbHelper.saveTrip("Maken reissu", null, null, dateFormat.format(this.startDate), null, null, null, null, null, null, null, null);
    }


    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public void stopCurrentTrip(){
        Log.e(TAG, "stopCurrentTrip: 1." );
        CommunicationHandler.getCommunicationHandlerInstance().setRunningStatus(false);
        this.endDate = new Date();
        setTripTimeTotal(formatDuration(endDate.getTime() - startDate.getTime()));
    }

    private String formatDuration(long tripTimeTotal) {
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(tripTimeTotal),
                TimeUnit.MILLISECONDS.toMinutes(tripTimeTotal) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(tripTimeTotal) % TimeUnit.MINUTES.toSeconds(1));
        Log.e(TAG, "formatDuration: time formatted " + hms);
        return hms;
    }


    public void saveTripToDb(){
        Log.e(TAG, "saveTripToDb: 4." );
        this.tripDbHelper.endTrip(this.tripId, this.getTotalDistance(), dateFormat.format(this.endDate), getTripTimeTotal(), this.getAverageSpeed(), this.getAverageRPM(), null, null, null, null);
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
