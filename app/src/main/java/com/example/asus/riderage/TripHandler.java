package com.example.asus.riderage;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormat;

import com.example.asus.riderage.Database.TripDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 30/09/2016.
 */

public class TripHandler {
    private TripObject currentTrip;
    private long tripId;
    private TripDatabaseHelper tripDbHelper;
    private DateFormat dateFormat;
    private long currentTripId;
    private Date startDate, endDate;
    private long tripTimeTotal;

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
        this.currentTripId = this.tripDbHelper.saveTrip("Maken reissu", null, null, dateFormat.format(startDate), null, null, null, null, null, null, null);
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
        this.endDate = new Date();
        this.tripTimeTotal = endDate.getTime() - startDate.getTime();
        BluetoothManagerClass.getBluetoothManagerClass().closeSocket();
        //TODO: step numero 3
        Log.e(TAG, "stopCurrentTrip: TRIP ENDED:\n TIME TAKEN: " + TimeUnit.MILLISECONDS.toSeconds(this.tripTimeTotal));
    }
}
