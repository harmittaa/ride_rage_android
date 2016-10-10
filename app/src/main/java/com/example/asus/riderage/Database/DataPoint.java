package com.example.asus.riderage.Database;


/**
 * DataPoint class, includes only construct, getters & setters
 */

public class DataPoint {
    private double speed,rpm;
    private long tripId;
    private double acceleration, consumption, longitude, latitude;
    private String timestamp;

    public DataPoint(long tripId, double spd, double r, double accel, double cons, double longitude, double latitude, String timestamp){
        this.speed = spd;
        this.rpm = r;
        this.acceleration = accel;
        this.consumption = cons;
        this.tripId = tripId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
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

    public double getLongitude() {return longitude;}

    public void setLongitude(double longitude) {this.longitude = longitude;}

    public double getLatitude() {return latitude;}

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
