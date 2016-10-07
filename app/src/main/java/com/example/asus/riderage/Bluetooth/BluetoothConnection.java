package com.example.asus.riderage.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.asus.riderage.R;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Created by Asus on 26/09/2016.
 */

// for making a connection between the user's device and the OBDII device

public class BluetoothConnection implements Callable<Boolean> {
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
    public Boolean call() throws Exception {
        try {
            this.btSocket = this.bluetoothManagerClass.getBluetoothSocket();
            this.btSocket = device.createRfcommSocketToServiceRecord(uuidToConnect);
            //Log.e(TAG, "Connecting with " + uuidToConnect.toString());
            this.btSocket.connect();
            this.bluetoothManagerClass.setBluetoothSocket(this.btSocket);
         //   startObdInit();
            //Log.e(TAG, "Connected with " + uuidToConnect.toString());
            return true;
        } catch (IOException e) {
            //Log.e(TAG, "call: " ,  e);
            return connectWithFallbackSocket();
        }
    }

    private boolean connectWithFallbackSocket() {
        try {
            //Log.e(TAG, "Trying fallback socket");
            this.btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
            //Log.e(TAG, "Fallback socket created, trying to connect...");
            this.btSocket.connect();
            this.bluetoothManagerClass.setBluetoothSocket(this.btSocket);
          //  startObdInit();
            //Log.e(TAG, "Connection established");
            return true;
        } catch (Exception e2) {
            //Log.e(TAG, "Couldn't establish Bluetooth connection!", e2);
            this.communicationHandler.makeToast(R.string.couldNotConnect);
            return false;
        }
    }
}
