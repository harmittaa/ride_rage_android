package com.example.asus.riderage;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Asus on 27/09/2016.
 */

// BG service to run the ObdJobs
public class ObdJobService extends Service implements SensorEventListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "ObdJobService";
    private BluetoothSocket bluetoothSocket;
    private BluetoothManagerClass bluetoothManagerClass;
    private CommunicationHandler communicationHandler;
    private Thread serviceThread;
    private boolean isAccelerationInProgress = false;
    private double speed, rpm, acceleration, consumption, averageSpeed, averageRpm, longitude, latitude;
    TripHandler tripHandler;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    volatile boolean isRunning = false;
    private ArrayList<Double> speeds,rpms;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ObdJobService created");
        speeds = new ArrayList<>();
        rpms = new ArrayList<>();
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        tripHandler = CommunicationHandler.getCurrentTripHandler();
        initGoogleApiClient();
        createLocationRequest();
        googleApiClient.connect();
        ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onStartCommand: ObdJobService started");
                bluetoothSocket = bluetoothManagerClass.getBluetoothSocket();
                Log.e(TAG, "onStartCommand: bluetooth socket connection is " + BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().isConnected());
                Log.e(TAG, "Starting thread to get RPM");
                final RPMCommand rpmCommand = new RPMCommand();
                SpeedCommand speedCommand = new SpeedCommand();

                Thread t = new Thread(new LoggerThread());
                t.start();
                isRunning = true;

                while (isRunning) {
                    try {
                        if (isRunning) {
                            Log.e(TAG, "onStartCommand: bluetooth socket connection is " + BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().isConnected());
                            speedCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                            rpmCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                            speed = Double.parseDouble(speedCommand.getCalculatedResult());
                            rpm = Double.parseDouble(rpmCommand.getCalculatedResult());
                            consumption = 10;
                            communicationHandler.updateGauges(rpm, speed);
                            Log.e(TAG, "RPM formatted " + rpm);
                            Log.e(TAG, "Speed formatted " + speed);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "ERROR running commands ", e);
                        closeConnection();
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                        return;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread interrupted " + e);
                        closeConnection();
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                        return;
                    }
                }
                closeConnection();
                CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
            }
        });
        serviceThread.start();
        return START_STICKY_COMPATIBILITY;
    }

    private void closeConnection() {
        try {
            if (BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().isConnected()) {
                ObdRawCommand rawCommand = new ObdRawCommand("AT PC");
                rawCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                Log.e(TAG, "closeConnection: OBD connection closed");
                isRunning = false;
            }
        } catch (Exception e) {
            Log.e(TAG, "closeConnection: error when closing connection ", e);
            isRunning = false;
        }

    }

    public void setRunning(boolean bool) {
        isRunning = bool;
    }

    // service is closed
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: called");
        this.setRunning(false);
        this.closeConnection();
        stopSelf();
    }

    // Accelerometer sensor gets new data
    @Override
    public void onSensorChanged(SensorEvent event) {
        for (double d : event.values) {
            if (d > 8) {
            } else if (0 < d && d < 8) {
                this.isAccelerationInProgress = true;
                acceleration = d;
                //Log.e(TAG, "onSensorChanged: Accel value " + d);
            } else {
                this.isAccelerationInProgress = false;
            }
        }
    }

    // Accelerometer sensor's accuracy has changed
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Binder for the service, not used because this is a manually started/stopped service
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*********
     * Getters and setters
     *********/

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getRpm() {
        return rpm;
    }

    public void setRpm(double rpm) {
        this.rpm = rpm;
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

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getAverageRpm() {
        return averageRpm;
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

    public void setAverageRpm(double averageRpm) {
        this.averageRpm = averageRpm;
    }

    // GOOGLE MAPS API starts here
    @Override
    public void onLocationChanged(Location location) {
        Log.e(TAG, "onLocationChanged: longitued " + location.getLongitude());
        Log.e(TAG, "onLocationChanged: latitude " + location.getLatitude());
        setLatitude(location.getLatitude());
        setLongitude(location.getLongitude());
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("In onconnected");
        try {
            Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, ObdJobService.this);

            Log.e(TAG, "onConnected: LATTARI"+loc.getLatitude() );
        } catch (SecurityException e) {
            e.printStackTrace();
            System.out.println("Security Failed");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended: Google maps suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: Google maps connection failed");
    }

    public void addToRpms(Double rpmToAdd){
        this.rpms.add(rpmToAdd);
    }

    public void addToSpeeds(Double speedToAdd){
        this.speeds.add(speedToAdd);
    }

    public ArrayList<Double> getRpms(){
        return this.rpms;
    }

    public ArrayList<Double> getSpeeds(){
        return this.speeds;
    }

    public Double getTotalOfDoubleArray(ArrayList<Double> arrayToCount){
        Double jeeben = 0.0;
        for (Double d : arrayToCount){
            jeeben += d;
        }
        return jeeben;
    }
    // Thread that logs all the data from the sensors
    private class LoggerThread implements Runnable {
        int counter;

        @Override
        public void run() {
            while (isRunning) {
                try {
                    Thread.sleep(2000);
                    counter++;
                    addToRpms(getRpm());
                    setAverageRpm(getTotalOfDoubleArray(getRpms())/getRpms().size());
                    setAverageSpeed(getTotalOfDoubleArray(getSpeeds())/getSpeeds().size());
                    tripHandler.storeDataPointToDB(new DataPoint(tripHandler.getTripId(), getSpeed(), getRpm(), getAcceleration(), getConsumption(), getLongitude(), getLatitude()));
                    if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "LoggerThread interupted ", e);
                    Thread.currentThread().interrupt();
                    Thread.interrupted();
                    return;
                }
            }
            Log.e(TAG, "run: Logger thread should end now, passing averages");
            tripHandler.setAverageSpeed(getAverageSpeed());
            tripHandler.setAverageRPM(getAverageRpm());
        }
    }
}