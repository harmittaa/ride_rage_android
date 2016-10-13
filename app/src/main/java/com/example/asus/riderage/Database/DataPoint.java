package com.example.asus.riderage.Database;


/**
 * DataPoint class, includes construct, getters & setters
 */

public class DataPoint {
    private double speed,rpm;
    private long tripId;
    private double acceleration, consumption, longitude, latitude;
    private String timestamp;

    /**
     * Constructor for the DataPoint
     * @param tripId ID for the trip which this DP is related to
     * @param spd Speed in KM/H for this DP
     * @param r RPM for this DP
     * @param accel Acceleration for this DP
     * @param cons Consumption for this DP
     * @param longitude Longitude for this DP
     * @param latitude Latitude for this DP
     * @param timestamp Timestamp for this DP
     */
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

    /*
     * GETTERS AND SETTERS
     */

    double getAcceleration() {
        return acceleration;
    }

    double getConsumption() {
        return consumption;
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

    long getTripId() {return tripId;}

    double getLongitude() {return longitude;}

    double getLatitude() {return latitude;}

    String getTimestamp() {
        return timestamp;
    }
}
