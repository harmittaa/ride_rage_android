package com.example.asus.riderage;

/**
 * Created by Daniel on 30/09/2016.
 */

public class TripHandler {
    private TripObject currentTrip;

    public TripHandler(){

    }

    public void storeDataPointToDB(DataPoint dataPoint){
        //TODO get relevant values from datapoint, store into DBHelper
    }

    public TripObject getCurrentTrip() {
        return currentTrip;
    }

    public void setCurrentTrip(TripObject currentTrip) {
        this.currentTrip = currentTrip;
    }
}
