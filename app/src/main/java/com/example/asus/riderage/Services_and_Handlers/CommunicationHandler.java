package com.example.asus.riderage.Services_and_Handlers;

import android.util.Log;

import com.example.asus.riderage.Bluetooth.BluetoothManagerClass;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


/**
 * Singleton that handles communication between classes, holds instances of classes like TripHandler and DataVariable
 */
public class CommunicationHandler {
    private static final String TAG = "CommunicationHandler";
    private static CommunicationHandler communicationHandlerInstance = new CommunicationHandler();
    private MainActivity mainActivity;
    private BluetoothManagerClass btManager;
    private DataVariables dataVariable;
    private static TripHandler currentTripHandler;
    private long tripId;

    private Constants.CONNECTION_STATE connection_state = Constants.CONNECTION_STATE.DISCONNECTED;
    private volatile boolean runningStatus = false;
    private String tripName;

    private CommunicationHandler() {
        this.btManager = BluetoothManagerClass.getBluetoothManagerClass();
        this.connection_state = Constants.CONNECTION_STATE.DISCONNECTED;
    }

    /**
     * Used to pass the context to the CommunicationHandler
     * @param ma MainActivity
     */
    public void passContext(MainActivity ma) {
        this.mainActivity = ma;
    }

    public MainActivity getContext() {
        return this.mainActivity;
    }

    /**
     * @return CommunicationHandler singleton instance
     */
    public static CommunicationHandler getCommunicationHandlerInstance() {
        return communicationHandlerInstance;
    }

    public boolean checkBluetoothStatus(boolean withPrompt) {
        return this.btManager.checkBluetoothIsOn(withPrompt);
    }

    public ArrayList<String> getDeviceStrings() {
        return this.btManager.getDeviceStrings();
    }


    public void makeToast(int couldNotConnect) {
        mainActivity.makeToast(mainActivity.getString(couldNotConnect));
    }

    public void startObdJobService() {
        mainActivity.startObdJobService();
    }

    public void updateGauges(double rpm, double speed) {
        mainActivity.updateGauges(rpm, speed);
    }

    public boolean bluetoothSocketIsConnected() {
        return btManager.bluetoothIsConnected();
    }

    /**
     * Creates a future task which runs ObdInitializer, when returning true starts the trip,
     * otherwise shows pop up to user.
     * @return False if the initialization cannot be made, true if it can be made.
     */
    public boolean checkSafeConnection() {
        if (checkBluetoothStatus(false)) {
            if (bluetoothSocketIsConnected()) {
                FutureTask<Boolean> futureTask = new FutureTask<>(new ObdInitializer());
                Thread t = new Thread(futureTask);
                t.start();
                try {
                    if (futureTask.get()) {
                        createTripHandler();
                        startObdJobService();
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.CONNECTED_RUNNING);
                        return true;
                    } else {
                        getContext().makeToast("Could not start OBD device");
                        return false;
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "checkSafeConnection: interrupt", e);
                } catch (ExecutionException e) {
                    Log.e(TAG, "checkSafeConnection:execution ", e);
                }
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

    public Constants.CONNECTION_STATE getConnection_state() {
        return connection_state;
    }

    public void setConnection_state(Constants.CONNECTION_STATE connection_state) {
        this.connection_state = connection_state;
        this.getContext().updateOnConnectionStateChanged(this.connection_state);
    }

    public long getTripId() {
        return tripId;
    }

    public void setTripId(long tripId) {
        this.tripId = tripId;
    }

    public DataVariables getDataVariable() {
        return dataVariable;
    }

    public void setDataVariable(DataVariables dataVariable) {
        this.dataVariable = dataVariable;
    }

    public boolean getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(boolean runningStatus) {
        this.runningStatus = runningStatus;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getTripName() {
        return tripName;
    }
}
