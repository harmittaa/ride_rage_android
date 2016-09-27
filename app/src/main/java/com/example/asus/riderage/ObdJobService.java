package com.example.asus.riderage;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;

import java.io.IOException;

/**
 * Created by Asus on 27/09/2016.
 */

public class ObdJobService extends Service {
    private static final String TAG = "ObdJobService";
    private BluetoothSocket bluetoothSocket;
    private BluetoothManagerClass bluetoothManagerClass;
    private Thread serviceThread;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ObdJobService created");
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
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

                for (int i = 0; i < 100; i++) {
                    try {
                        speedCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                        Log.e(TAG, "RPM formatted " + rpmCommand.getFormattedResult());
                        Log.e(TAG, "Speed formatted " + speedCommand.getFormattedResult());

                    } catch (Exception e) {
                        Log.e(TAG, "ERROR running commands ", e);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Thread sleep: error ", e);
                        return;
                    }
                }
            }
        });
        serviceThread.start();
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: called");
        this.serviceThread.interrupt();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
