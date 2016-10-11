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
import com.example.asus.riderage.Fragments.ResultFragment;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Database.DataPoint;
import com.example.asus.riderage.Misc.TripDataParser;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
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

    /**
     * Creates LocationRequest to start receiving updates from LocationListener
     */
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
                CommunicationHandler.getCommunicationHandlerInstance().setRunningStatus(true);

                while (communicationHandler.getRunningStatus()) {
                    try {
                        // OBD commands to be executed
                        speedCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                        rpmCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                        dataVariable.setSpeed(Double.parseDouble(speedCommand.getCalculatedResult()));
                        dataVariable.setRpm(Double.parseDouble(rpmCommand.getCalculatedResult()));
                        dataVariable.setConsumption(10);
                        communicationHandler.updateGauges(dataVariable.getRpm(), dataVariable.getSpeed());

                    } catch (IOException e) {
                        Log.e(TAG, "ERROR running commands ", e);
                        closeConnection();
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopObdJobService();
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().stopLoggerService();
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                        return;
                    } catch (InterruptedException e) {
                        Log.e(TAG, "ERROR running commands", e);
                        closeConnection();
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
                // trip has been ended, saving averages to tripHandler
                tripHandler.setAverageSpeed(dataVariable.getAvgSpeed());
                tripHandler.setAverageRPM(dataVariable.getAvgRpm());
                tripHandler.setTotalDistance(dataVariable.getTotalDistance());
                CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
                tripHandler.saveTripToDb();
                /*TripDataParser dataParser = new TripDataParser();
                dataParser.execute();*/
                CommunicationHandler.getCommunicationHandlerInstance().getContext().changeVisibleFragmentType(Constants.FRAGMENT_TYPES.RESULT_FRAGMENT,true);
                stopSelf();
            }
        });
        serviceThread.start();
        Log.e(TAG, "onStartCommand: should start loggerservice now" );
        startService(new Intent(CommunicationHandler.getCommunicationHandlerInstance().getContext(), LoggerService.class));
        return START_STICKY_COMPATIBILITY;
    }

    /**
     * Executes the closing command to the OBDII
     */
    private void closeConnection() {
        try {
            if (BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().isConnected()) {
                TripDataParser dataParser = new TripDataParser();
                dataParser.execute();
                ObdRawCommand rawCommand = new ObdRawCommand("AT PC");
                rawCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                Log.e(TAG, "closeConnection: 3.");
            }
        } catch (Exception e) {
            Log.e(TAG, "closeConnection: error when closing connection ", e);
        }

    }

    /**
     * Receives call when the ObdJobService is going to be closed, starts closing process
     */
    @Override
    public void onDestroy() {
        communicationHandler.setRunningStatus(false);
        this.closeConnection();
        stopLocationUpdates();
        stopService(new Intent(this, LoggerService.class));
        stopSelf();
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    /**
     * Receives new events from the accelerometer sensor
     * @param event
     */
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    /**
     * Binder for the service, not used because this is a manually started/stopped service
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*********
     * GOOGLE MAPS API starts here
     *********/

    /**
     * Called when location when a new location is received, calculates total length from the location objects
     * @param location Current location
     */
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
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}