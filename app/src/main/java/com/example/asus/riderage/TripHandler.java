package com.example.asus.riderage;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;

import com.example.asus.riderage.Database.TripDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Daniel on 30/09/2016.
 */

public class TripHandler {
    private TripObject currentTrip;
    private long tripId;
    private TripDatabaseHelper tripDbHelper;
    private DateFormat dateFormat;
    private long currentTripId;

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
        Date date = new Date();
        this.currentTripId = this.tripDbHelper.saveTrip("Maken reissu", null, null, dateFormat.format(date), null, null, null, null, null, null, null);
    }


    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }
}
