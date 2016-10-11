package com.example.asus.riderage.Services_and_Handlers;

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

import com.example.asus.riderage.Bluetooth.BluetoothManagerClass;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Database.DataPoint;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private DataVariables dataVariable;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private ArrayList<Double> speeds, rpms;
    private double totalDistance;
    private Location previousLocation;

    @Override
    public void onCreate() {
        speeds = new ArrayList<>();
        rpms = new ArrayList<>();
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        this.dataVariable = new DataVariables();
        this.communicationHandler.setDataVariable(this.dataVariable);

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
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                bluetoothSocket = bluetoothManagerClass.getBluetoothSocket();
                final RPMCommand rpmCommand = new RPMCommand();
                SpeedCommand speedCommand = new SpeedCommand();

                /*Thread t = new Thread(new LoggerThread());
                t.start();*/
                CommunicationHandler.getCommunicationHandlerInstance().setRunningStatus(true);

                while (communicationHandler.getRunningStatus()) {
                    try {
                        speedCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                        rpmCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                        dataVariable.setSpeed(Double.parseDouble(speedCommand.getCalculatedResult()));
                        dataVariable.setRpm(Double.parseDouble(rpmCommand.getCalculatedResult()));
                        dataVariable.setConsumption(10);
                        communicationHandler.updateGauges(dataVariable.getRpm(), dataVariable.getSpeed());

                    } catch (Exception e) {
                        Log.e(TAG, "ERROR running commands ", e);
                        closeConnection();
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopObdJobService();
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopLoggerService();
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                        return;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread interrupted " + e);
                        closeConnection();
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopObdJobService();
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopLoggerService();
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                        return;
                    }
                }
                Log.e(TAG, "ObdJobService: 2.");
                tripHandler.setAverageSpeed(dataVariable.getAvgSpeed());
                tripHandler.setAverageRPM(dataVariable.getAvgRpm());
                tripHandler.setTotalDistance(getTotalDistance());
                CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                tripHandler.saveTripToDb();
                CommunicationHandler.getCommunicationHandlerInstance().getContext().changeVisibleFragmentType(Constants.FRAGMENT_TYPES.RESULT_FRAGMENT,true);
                Log.e(TAG, "stopself: 5.");
                stopSelf();
            }
        });
        serviceThread.start();
        Log.e(TAG, "onStartCommand: should start loggerservice now" );
        startService(new Intent(CommunicationHandler.getCommunicationHandlerInstance().getContext(), LoggerService.class));
        return START_STICKY_COMPATIBILITY;
    }

    private void closeConnection() {
        try {
            if (BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().isConnected()) {
                ObdRawCommand rawCommand = new ObdRawCommand("AT PC");
                rawCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                Log.e(TAG, "closeConnection: 3.");
            }
        } catch (Exception e) {
            Log.e(TAG, "closeConnection: error when closing connection ", e);
        }

    }


    // service is closed
    @Override
    public void onDestroy() {
        communicationHandler.setRunningStatus(false);
        this.closeConnection();
        stopLocationUpdates();
        stopService(new Intent(this, LoggerService.class));
        stopSelf();
    }

    private void stopLocationUpdates() {
        //Log.e(TAG, "stopLocationUpdates: ending location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    // Accelerometer sensor gets new data
    @Override
    public void onSensorChanged(SensorEvent event) {
        for (double d : event.values) {
            if (d > 8) {
            } else if (0 < d && d < 8) {
                this.isAccelerationInProgress = true;
                acceleration = d;
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

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void addToRpms(Double rpmToAdd) {
        this.rpms.add(rpmToAdd);
    }

    public void addToSpeeds(Double speedToAdd) {
        this.speeds.add(speedToAdd);
    }

    public ArrayList<Double> getRpms() {
        return this.rpms;
    }

    public ArrayList<Double> getSpeeds() {
        return this.speeds;
    }

    // GOOGLE MAPS API starts here
    @Override
    public void onLocationChanged(Location location) {
        if (CommunicationHandler.getCommunicationHandlerInstance().getRunningStatus()) {
            dataVariable.setLatitude(location.getLatitude());
            dataVariable.setLongitude(location.getLongitude());
            if (this.previousLocation != null) {
                dataVariable.setTotalDistance(dataVariable.getTotalDistance() + location.distanceTo(this.previousLocation) / 1000);
                CommunicationHandler.getCommunicationHandlerInstance().getContext().updateDistanceTextView(dataVariable.getTotalDistance());
            }
            this.previousLocation = location;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("In onconnected");
        try {
            Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, ObdJobService.this);

        } catch (SecurityException e) {
            e.printStackTrace();
            System.out.println("Security Failed");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.e(TAG, "onConnectionSuspended: Google maps suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Log.e(TAG, "onConnectionFailed: Google maps connection failed");
    }

    public Double getTotalOfDoubleArray(ArrayList<Double> arrayToCount) {
        Double jeeben = 0.0;
        for (Double d : arrayToCount) {
            jeeben += d;
        }
        return jeeben;
    }


    /**
     * Creates DataPoints
     */
    private class LoggerThread implements Runnable {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        @Override
        public void run() {
            while (communicationHandler.getRunningStatus()) {
                if (getSpeed() > -1) {
                    try {
                        Thread.sleep(2000);
                        addToRpms(getRpm());
                        addToSpeeds(getSpeed());
                        setAverageRpm(getTotalOfDoubleArray(getRpms()) / getRpms().size());
                        setAverageSpeed(getTotalOfDoubleArray(getSpeeds()) / getSpeeds().size());
                        Log.e(TAG, "obdloggerthread: averages set to " + (getTotalOfDoubleArray(getSpeeds()) / getSpeeds().size()) + " & " + (getTotalOfDoubleArray(getRpms()) / getRpms().size()));
                        tripHandler.storeDataPointToDB(new DataPoint(tripHandler.getTripId(), getSpeed(), getRpm(), getAcceleration(), getConsumption(), getLongitude(), getLatitude(), dateFormat.format(new Date())));
                        if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                    } catch (InterruptedException e) {
                        //Log.e(TAG, "LoggerThread interupted ", e);
                        Thread.currentThread().interrupt();
                        Thread.interrupted();
                        return;
                    }
                }
            }
        }
    }
}