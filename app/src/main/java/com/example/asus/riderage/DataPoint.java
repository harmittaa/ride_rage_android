package com.example.asus.riderage;

/**
 * Created by Daniel on 30/09/2016.
 */

public class DataPoint {
    private double speed,rpm;
    private long tripId;
    private double acceleration,consumption;

    public DataPoint(long tripId, double spd, double r, double accel, double cons){
        this.speed = spd;
        this.rpm = r;
        this.acceleration = accel;
        this.consumption = cons;
        this.tripId = tripId;
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

    public double getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public long getTripId() {return tripId;}

    public void setTripId(long tripId) {this.tripId = tripId;}
}
