package com.example.asus.riderage;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Asus on 27/09/2016.
 */

public class CommunicationHandler {
    private static final String TAG = "CommunicationHandler";
    private static CommunicationHandler communicationHandlerInstance = new CommunicationHandler();
    private MainActivity mainActivity;
    private BluetoothManagerClass btManager;
    private boolean accelInProgress = false;

    private CommunicationHandler() {
        this.btManager = BluetoothManagerClass.getBluetoothManagerClass();
        Log.e(TAG, "CommunicationHandler: created");
    }

    public void passContext(MainActivity ma) {
        this.mainActivity = ma;
    }

    public MainActivity getContext() {
        return this.mainActivity;
    }

    public static CommunicationHandler getCommunicationHandlerInstance() {
        return communicationHandlerInstance;
    }

    public boolean checkBluetoothStatus() {
        return this.btManager.checkBluetooth();
    }

    public ArrayList<String> getDeviceStrings() {
       return this.btManager.getDeviceStrings();
    }

    public void createBluetoothConnection(int position) {
        this.btManager.createBluetoothConnection(position);
    }

    public void makeToast(int couldNotConnect) {
        Log.e(TAG, "makeToast: " + mainActivity.getString(couldNotConnect));
        mainActivity.makeToast(mainActivity.getString(couldNotConnect));
    }

    public void startObdJobService() {
        mainActivity.startObdJobService();
    }

    public void stopObdJobService() {
        mainActivity.stopObdJobService();
    }

    public void updateGauges(double rpm, double speed) {
        mainActivity.updateGauges(rpm, speed);
    }

    public boolean isAccelInProgress() {
        return accelInProgress;
    }

    public void setAccelInProgress(boolean accelInProgress) {
        Log.e(TAG, "setAccelInProgress:  kakke" );
        this.accelInProgress = accelInProgress;
    }
}
