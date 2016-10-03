package com.example.asus.riderage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Asus on 26/09/2016.
 */

public class BluetoothManagerClass {
    private static final String TAG = "BluetoothManagerClass";
    private static BluetoothManagerClass bluetoothManagerClass = new BluetoothManagerClass();
    private static BluetoothSocket bluetoothSocket;
    private CommunicationHandler communicationHandler;
    private BluetoothAdapter btAdapter;
    private ArrayList<String> deviceStrs;
    private ArrayList<BluetoothDevice> devices;

    private BluetoothManagerClass() {
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.e(TAG, "BluetoothManagerClass: BT manager created");
    }

    public static BluetoothManagerClass getBluetoothManagerClass() {
        return bluetoothManagerClass;
    }

    public boolean checkBluetoothIsOn(boolean withPrompt) {
        if (!this.btAdapter.isEnabled()) {
            if(withPrompt) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                CommunicationHandler.getCommunicationHandlerInstance().getContext().startActivityForResult(enableBtIntent, RESULT_OK);
            }
            return false;
        } else return true;
    }


    public ArrayList<String> getDeviceStrings() {
        deviceStrs = new ArrayList();
        devices = new ArrayList();
        Set<BluetoothDevice> pairedDevices = this.btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }
        }
        return this.deviceStrs;
    }

    public boolean createBluetoothConnection(int position) {
        String deviceAddress = devices.get(position).getAddress();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        UUID uuid = UUID.fromString(devices.get(position).getUuids()[0].toString());
        Log.e(TAG, "createBluetoothConnection: the UUID is" + uuid.toString());
        Log.e(TAG, "createBluetoothConnection: the UUID should be 00001101-0000-1000-8000-00805f9b34fb");

        FutureTask<Boolean> futureTask = new FutureTask<>(new BluetoothConnection(uuid, device));
        Thread t=new Thread(futureTask);
        t.start();
        try {
            return futureTask.get().booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //with thread instead of callable
        /*Thread connectionCreationThread = new Thread(new BluetoothConnection(uuid, device));
        connectionCreationThread.start();*/
    }

    protected BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public boolean bluetoothIsConnected(){
        return bluetoothSocket.isConnected();
    }

    protected void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        Log.e(TAG, "setBluetoothSocket: Something is setting the BT socket, socket connection status is  " + bluetoothSocket.isConnected());
        BluetoothManagerClass.bluetoothSocket = bluetoothSocket;
    }
}
