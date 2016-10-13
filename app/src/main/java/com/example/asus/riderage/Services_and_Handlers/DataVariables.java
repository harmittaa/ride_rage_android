package com.example.asus.riderage.Services_and_Handlers;

import android.util.Log;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Holds variables such as rpm, speed and averages for the trip
 */

class DataVariables {
    private double rpm, speed, avgSpeed, avgRpm, acceleration, consumption, longitude, latitude, totalDistance;
    private ArrayList<Double> rpmList, speedList;

    DataVariables() {
        this.speedList = new ArrayList<>();
        this.rpmList = new ArrayList<>();
    }

    public double getRpm() {
        return rpm;
    }

    public void setRpm(double rpm) {
        this.rpm = rpm;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    void addToSpeed(double speed) {
        this.speedList.add(speed);
    }

    void addToRpm(double rpm) {
        this.rpmList.add(rpm);
    }


    double getAvgRpm() {
        return avgRpm;
    }

    void setAvgRpm(double avgRpm) {
        this.avgRpm = avgRpm;
        Log.e(TAG, "setAvgRpm: avgrpm set to " + this.avgRpm );
    }

    double getAvgSpeed() {
        return avgSpeed;
    }

    void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
        Log.e(TAG, "setAvgSpeed: avgspd set to " +this.avgSpeed);
    }

    ArrayList<Double> getRpmList() {
        return rpmList;
    }

    ArrayList<Double> getSpeedList() {
        return speedList;
    }

    double getAcceleration() {
        return acceleration;
    }

    double getConsumption() {
        return consumption;
    }

    void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double getTotalDistance() {
        return totalDistance;
    }

    void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
}
