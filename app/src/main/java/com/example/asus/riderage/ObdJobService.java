package com.example.asus.riderage;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.protocol.ObdRawCommand;

import java.io.IOException;

/**
 * Created by Asus on 27/09/2016.
 */

// BG service to run the ObdJobs
public class ObdJobService extends Service implements SensorEventListener {
    private static final String TAG = "ObdJobService";
    private BluetoothSocket bluetoothSocket;
    private BluetoothManagerClass bluetoothManagerClass;
    private CommunicationHandler communicationHandler;
    private Thread serviceThread;
    private boolean isAccelerationInProgress = false;
    private double speed, rpm, acceleration, consumption;
    TripHandler tripHandler;
    volatile boolean isRunning = false;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ObdJobService created");
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        //i like spaghetti
        tripHandler = CommunicationHandler.getCurrentTripHandler();
        ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
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
                        if(isRunning) {
                            speedCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                            rpmCommand.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
                            speed = Double.parseDouble(speedCommand.getCalculatedResult());
                            rpm = Double.parseDouble(rpmCommand.getCalculatedResult());
                            consumption = 10;
                            communicationHandler.updateGauges(rpm, speed);
                            Log.e(TAG, "RPM formatted " + rpm);
                            Log.e(TAG, "Speed formatted " + speed);
                            Log.e(TAG, "Consumption formatted " + consumption);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "ERROR running commands ", e);
                        closeConnection();
                        return;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread interrupted " + e);
                        closeConnection();
                        return;
                    }
                }
                closeConnection();
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
                /*
                 * Moved closing of actual socket to BluetoothManagerClass, only closing command to OBD remains
                 */
                //Log.e(TAG, "Raw command returns " + rawCommand.getFormattedResult());
                //BluetoothManagerClass.getBluetoothManagerClass().closeSocket();
                //Log.e(TAG, "closeConnection: connection closed");
                //this.bluetoothManagerClass.setBluetoothSocket(this.bluetoothSocket);
                Log.e(TAG, "closeConnection: OBD connection closed");
            }
        } catch (Exception e) {
            Log.e(TAG, "closeConnection: error when closing connection ", e);
        }

    }

    public void setRunning(boolean bool) {
        isRunning = bool;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: called");
        this.setRunning(false);
        this.closeConnection();
        stopSelf();
    }

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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


    private class LoggerThread implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    Thread.sleep(2000);
                    tripHandler.storeDataPointToDB(new DataPoint(tripHandler.getTripId(), getSpeed(), getRpm(), getAcceleration(), getConsumption()));
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
            Log.e(TAG, "run: Logger thread should end now");

        }
    }
}