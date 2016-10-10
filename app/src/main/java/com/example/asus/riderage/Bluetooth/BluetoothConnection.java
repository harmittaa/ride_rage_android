package com.example.asus.riderage.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.asus.riderage.R;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Class is used for establishing connection to OBD device
 */

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

    /**
     * Tries to create a normal RfCommSocket connection, if it fails calls fallback method
     * @throws Exception
     */
    @Override
    public Boolean call() throws Exception {
        try {
            this.btSocket = this.bluetoothManagerClass.getBluetoothSocket();
            this.btSocket = device.createRfcommSocketToServiceRecord(uuidToConnect);
            this.btSocket.connect();
            this.bluetoothManagerClass.setBluetoothSocket(this.btSocket);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IOException while connecting: " ,  e);
            return connectWithFallbackSocket();
        }
    }

    /**
     * Fallback connection method in case createRfcommSocketToServiceRecord connection fails.
     * Uses hidden method "crteRfcommScoket" instead
     */
    private boolean connectWithFallbackSocket() {
        try {
            this.btSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
            this.btSocket.connect();
            this.bluetoothManagerClass.setBluetoothSocket(this.btSocket);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Couldn't establish Bluetooth connection!", e);
            this.communicationHandler.makeToast(R.string.couldNotConnect);
            return false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }
}
