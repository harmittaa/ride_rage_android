package com.example.asus.riderage.Services_and_Handlers;

import android.util.Log;

import com.example.asus.riderage.Bluetooth.BluetoothManagerClass;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.MainActivity;

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
    private DataVariables dataVariable;
    private static TripHandler currentTripHandler;
    private long tripId;

    private Constants.CONNECTION_STATE connection_state;
    private CommunicationHandler() {
        this.btManager = BluetoothManagerClass.getBluetoothManagerClass();
        //Log.e(TAG, "CommunicationHandler: created");
        this.connection_state = Constants.CONNECTION_STATE.DISCONNECTED;
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
        boolean jeeben = this.btManager.createBluetoothConnection(position);
        if (jeeben) {
            setConnection_state(Constants.CONNECTION_STATE.CONNECTED_NOT_RUNNING);
        }
        return jeeben;
    }

    public void makeToast(int couldNotConnect) {
        //Log.e(TAG, "makeToast: " + mainActivity.getString(couldNotConnect));
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
                        //Log.e(TAG, "checkSafeConnection: createhanlder done" );
                        startObdJobService();
                        //Log.e(TAG, "checkSafeConnection: start obdservice done" );
                        CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.CONNECTED_RUNNING);
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
}
