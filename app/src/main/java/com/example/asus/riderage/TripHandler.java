package com.example.asus.riderage;

import android.content.Intent;
import android.util.Log;

import java.text.DateFormat;

import com.example.asus.riderage.Database.TripDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 30/09/2016.
 */

public class TripHandler {
    private TripObject currentTrip;
    private long tripId;
    private TripDatabaseHelper tripDbHelper;
    private DateFormat dateFormat;
    private Date startDate, endDate;
    private long tripTimeTotal;
    private double averageSpeed, averageRPM;

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
        //TODO 1. stop services and logging, disconnect socket 2. get duration of trip and store it here 3. save trip in database
        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopService(new Intent(CommunicationHandler.getCommunicationHandlerInstance().getContext(),ObdJobService.class));
        Log.e(TAG, "stopCurrentTrip: starting to log trip data");

        this.endDate = new Date();
        this.tripTimeTotal = endDate.getTime() - startDate.getTime();

        //BluetoothManagerClass.getBluetoothManagerClass().closeSocket();
        //TODO: step numero 3
        Log.e(TAG, "stopCurrentTrip: TRIP ENDED:\n TIME TAKEN: " + TimeUnit.MILLISECONDS.toSeconds(this.tripTimeTotal));
        this.tripDbHelper.endTrip(this.tripId, null, dateFormat.format(this.endDate), this.tripTimeTotal, this.getAverageSpeed(), this.getAverageRPM(), null, null, null, null);
        CommunicationHandler.getCommunicationHandlerInstance().getContext().setFragmentTripId(this.tripId);
        CommunicationHandler.getCommunicationHandlerInstance().getContext().changeVisibleFragmentType(Constants.FRAGMENT_TYPES.RESULT_FRAGMENT);
    }


    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getAverageRPM() {
        return averageRPM;
    }

    public void setAverageRPM(double averageRPM) {
        this.averageRPM = averageRPM;
    }
}
