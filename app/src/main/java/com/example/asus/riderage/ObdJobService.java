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
import com.github.pires.obd.commands.protocol.ObdRawCommand;

import java.io.IOException;

/**
 * Created by Asus on 27/09/2016.
 */

public class ObdJobService extends Service implements SensorEventListener {
    private static final String TAG = "ObdJobService";
    private BluetoothSocket bluetoothSocket;
    private BluetoothManagerClass bluetoothManagerClass;
    private CommunicationHandler communicationHandler;
    private Thread serviceThread;
    private boolean isAccelerationInProgress = false;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ObdJobService created");
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        //i like spaghetti
        ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.serviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onStartCommand: ObdJobService started");
                bluetoothSocket = bluetoothManagerClass.getBluetoothSocket();

                Log.e(TAG, "onStartCommand: bluetooth socket connection is " + bluetoothSocket.isConnected());

                Log.e(TAG, "Starting thread to get RPM");
                final RPMCommand rpmCommand = new RPMCommand();
                SpeedCommand speedCommand = new SpeedCommand();

                while (!Thread.interrupted()) {
                    try {
                        speedCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        communicationHandler.updateGauges(Double.parseDouble(rpmCommand.getCalculatedResult()),
                                Double.parseDouble(speedCommand.getCalculatedResult()));
                        Log.e(TAG, "RPM formatted " + rpmCommand.getFormattedResult());
                        Log.e(TAG, "Speed formatted " + speedCommand.getFormattedResult());


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
            if (this.bluetoothSocket.isConnected()) {
                ObdRawCommand rawCommand = new ObdRawCommand("AT PC");
                rawCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                Log.e(TAG, "Raw command returns " + rawCommand.getFormattedResult());
                this.bluetoothSocket.close();
                Log.e(TAG, "closeConnection: connection closed");
                this.bluetoothManagerClass.setBluetoothSocket(this.bluetoothSocket);
            }
        } catch (Exception e) {
            Log.e(TAG, "closeConnection: error when closing connection ", e);
        }

    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: called");
        this.serviceThread.interrupt();
        stopSelf();

        // TODO: 27/09/2016  this works for the BT conneciton, but the OBD2 still declines any incoming commmands after reconnect
        // java.io.IOException: bt socket closed, read return: -1
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for(double d : event.values){
            if (d>8){

            }else if (0<d && d<8){
                this.isAccelerationInProgress = true;
                Log.e(TAG, "onSensorChanged: Accel value " + d);
            }else {
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
}
