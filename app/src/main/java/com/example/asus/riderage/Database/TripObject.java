package com.example.asus.riderage.Database;

import java.util.Date;

/**
 * TripObject class includes empty constructor, getters & setters
 */

public class TripObject {
    private int tripID;
    private String carString;
    private Date startDate,endDate;
    private double avgConsumption;

    public TripObject(){

    }

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
    }

    public String getCarString() {
        return carString;
    }

    public void setCarString(String carString) {
        this.carString = carString;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getAvgConsumption() {
        return avgConsumption;
    }

    public void setAvgConsumption(double avgConsumption) {
        this.avgConsumption = avgConsumption;
    }
}
