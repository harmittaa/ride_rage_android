package com.example.asus.riderage;

/**
 * Created by Daniel on 30/09/2016.
 */

public class DataPoint {
    private int speed,rpm;
    private double acceleration,consumption;

    public DataPoint(int spd, int r, double accel, double cons){
        this.speed = spd;
        this.rpm = r;
        this.acceleration = accel;
        this.consumption = cons;
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

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
