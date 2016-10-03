package com.example.asus.riderage;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by Asus on 27/09/2016.
 */

public class CommunicationHandler {
    private static final String TAG = "CommunicationHandler";
    private static CommunicationHandler communicationHandlerInstance = new CommunicationHandler();
    private MainActivity mainActivity;
    private BluetoothManagerClass btManager;
    private static TripHandler currentTripHandler;

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

    public boolean checkBluetoothStatus(boolean withPrompt) {
        return this.btManager.checkBluetoothIsOn(withPrompt);
    }

    public ArrayList<String> getDeviceStrings() {
        return this.btManager.getDeviceStrings();
    }

    public boolean createBluetoothConnection(int position) {
        return this.btManager.createBluetoothConnection(position);
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

    public boolean bluetoothSocketIsConnected() {
        return btManager.bluetoothIsConnected();
    }

    public boolean checkSafeConnection() {
        if (checkBluetoothStatus(false)) {
            if (bluetoothSocketIsConnected()) {
                FutureTask<Boolean> futureTask = new FutureTask<>(new ObdInitializer());
                Thread t = new Thread(futureTask);
                t.start();
                try {
                    if (futureTask.get()) {
                        createTripHandler();
                        Log.e(TAG, "checkSafeConnection: createhanlder done" );
                        startObdJobService();
                        Log.e(TAG, "checkSafeConnection: start obdservice done" );
                        return true;
                    } else {
                        //TODO popup for user "error occured"
                        return false;
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "checkSafeConnection: interrupt" , e );
                } catch (ExecutionException e) {
                    Log.e(TAG, "checkSafeConnection:execution ", e );}
            }
            return false;
        }
        return false;
    }

    private void createTripHandler() {
        setCurrentTripHandler(new TripHandler());
        this.currentTripHandler.startNewTrip();
    }

    public static TripHandler getCurrentTripHandler() {
        return currentTripHandler;
    }

    public void setCurrentTripHandler(TripHandler currentTripHandler) {
        CommunicationHandler.currentTripHandler = currentTripHandler;
    }
}
