package com.example.asus.riderage.Services_and_Handlers;

import java.util.ArrayList;

/**
 * Holds variables such as rpm, speed and averages for the trip
 */

public class DataVariables {
    private double rpm, speed, avgSpeed, avgRpm, acceleration, consumption, longitude, latitude, totalDistance;
    private ArrayList<Double> rpmList, speedList;

    public DataVariables() {
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

    public void addToSpeed(double speed) {
        this.speedList.add(speed);
    }

    public void addToRpm(double rpm) {
        this.rpmList.add(rpm);
    }


    public double getAvgRpm() {
        return avgRpm;
    }

    public void setAvgRpm(double avgRpm) {
        this.avgRpm = avgRpm;
    }

    public double getAvgSpeed() {
        return avgSpeed;
    }

    public void setAvgSpeed(double avgSpeed) {
        this.avgSpeed = avgSpeed;
    }

    public ArrayList<Double> getRpmList() {
        return rpmList;
    }

    public void setRpmList(ArrayList<Double> rpmList) {
        this.rpmList = rpmList;
    }

    public ArrayList<Double> getSpeedList() {
        return speedList;
    }

    public void setSpeedList(ArrayList<Double> speedList) {
        this.speedList = speedList;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
}
