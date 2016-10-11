package com.example.asus.riderage.Services_and_Handlers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.asus.riderage.Database.DataPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Used to calculate averages and create datapoints during the trip
 */

public class LoggerService extends Service {
    private final String TAG = this.getClass().getName();
    private DataVariables dataVariable;
    TripHandler tripHandler;
    DateFormat dateFormat;

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        tripHandler = CommunicationHandler.getCurrentTripHandler();
        dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        dataVariable = CommunicationHandler.getCommunicationHandlerInstance().getDataVariable();
    }

    /**
     * Runs a thread which calculates averages and creates DataPoints to be saved to the Database.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread loggerServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(CommunicationHandler.getCommunicationHandlerInstance().getRunningStatus()) {
                    try {
                        Thread.sleep(2000);
                        dataVariable.addToRpm(dataVariable.getRpm());
                        dataVariable.addToSpeed(dataVariable.getSpeed());
                        dataVariable.setAvgRpm(getTotalOfDoubleArray(dataVariable.getRpmList()) / dataVariable.getRpmList().size());
                        dataVariable.setAvgSpeed(getTotalOfDoubleArray(dataVariable.getSpeedList()) / dataVariable.getSpeedList().size());
                        tripHandler.storeDataPointToDB(new DataPoint(tripHandler.getTripId(), dataVariable.getSpeed(), dataVariable.getRpm(), dataVariable.getAcceleration(),
                                dataVariable.getConsumption(), dataVariable.getLongitude(), dataVariable.getLatitude(), dateFormat.format(new Date())));
                        if (Thread.currentThread().isInterrupted() || Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Thread.interrupted();
                        return;
                    }
                }

            }
        });
        loggerServiceThread.start();
        return START_STICKY_COMPATIBILITY;
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Counts the combined value of doubles in a given ArrayList.
     * @param arrayToCount ArrayList holding doubles
     * @return The combined value of doubles in the ArrayList
     */
    public Double getTotalOfDoubleArray(ArrayList<Double> arrayToCount) {
        Double count = 0.0;
        for (Double d : arrayToCount) {
            count += d;
        }
        return count;
    }
}
