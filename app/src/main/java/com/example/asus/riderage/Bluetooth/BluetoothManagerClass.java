package com.example.asus.riderage.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.FutureTask;

import static android.app.Activity.RESULT_OK;

/**
 * Checks that bluetooth service is on, gets paired devices and holds BT socket
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
    }

    public static BluetoothManagerClass getBluetoothManagerClass() {
        return bluetoothManagerClass;
    }

    /**
     * Creates a prompt to enable bluetooth service if it's not enabled
     */
    public boolean checkBluetoothIsOn(boolean withPrompt) {
        if (!this.btAdapter.isEnabled()) {
            if(withPrompt) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                CommunicationHandler.getCommunicationHandlerInstance().getContext().startActivityForResult(enableBtIntent, RESULT_OK);
            }
            return false;
        } else return true;
    }


    /**
     * Gets paired device information
     * @return
     */
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

    /**
     * Handles calling BluetoothConnection and passing the correct bluetooth device selected by user
     */
    public boolean createBluetoothConnection(int position) {
        String deviceAddress = devices.get(position).getAddress();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
        UUID uuid = UUID.fromString(devices.get(position).getUuids()[0].toString());
        FutureTask<Boolean> futureTask = new FutureTask<>(new BluetoothConnection(uuid, device));
        Thread t=new Thread(futureTask);
        t.start();
        try {
            return futureTask.get().booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handles closing the BT socket
     */
    public void closeSocket(){
        try {
            if(this.getBluetoothSocket() != null) {
                this.getBluetoothSocket().close();
            }else Log.e(TAG, "closeSocket: Attempt to close socket that does not exist" );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public boolean bluetoothIsConnected(){
        return bluetoothSocket.isConnected();
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        BluetoothManagerClass.bluetoothSocket = bluetoothSocket;
    }
}
