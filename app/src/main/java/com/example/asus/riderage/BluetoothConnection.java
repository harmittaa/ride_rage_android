package com.example.asus.riderage;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Asus on 26/09/2016.
 */

// for making a connection between the user's device and the OBDII device

public class BluetoothConnection implements Runnable {
    private static final String TAG = "BluetoothConnection";
    private UUID uuidToConnect;
    private BluetoothDevice device;
    private BluetoothSocket btSocket;
    private BluetoothManagerClass bluetoothManagerClass;
    private CommunicationHandler communicationHandler;

    public BluetoothConnection(UUID uid, BluetoothDevice dev) {
        this.uuidToConnect = uid;
        this.device = dev;
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
    }

    @Override
    public void run() {
        try {
            this.btSocket = this.bluetoothManagerClass.getBluetoothSocket();
            this.btSocket = device.createRfcommSocketToServiceRecord(uuidToConnect);
            Log.e(TAG, "Connecting with " + uuidToConnect.toString());
            this.btSocket.connect();
            this.bluetoothManagerClass.setBluetoothSocket(this.btSocket);
            startObdInit();
            Log.e(TAG, "Connected with " + uuidToConnect.toString());
        } catch (IOException e) {
            Log.e(TAG, "ERROR", e);
            connectWithFallbackSocket();
        }
    }

    private void startObdInit() {
        Thread obdInitThread = new Thread(new ObdInitializer());
        obdInitThread.start();
    }

    private void connectWithFallbackSocket() {
        try {
            Log.e(TAG, "Trying fallback socket");
            this.btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
            Log.e(TAG, "Fallback socket created, trying to connect...");
            this.btSocket.connect();
            this.bluetoothManagerClass.setBluetoothSocket(this.btSocket);
            startObdInit();
            Log.e(TAG, "Connection established");
        } catch (Exception e2) {
            Log.e(TAG, "Couldn't establish Bluetooth connection!", e2);
            this.communicationHandler.makeToast(R.string.couldNotConnect);
        }
    }
}
