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
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        dataVariable = CommunicationHandler.getCommunicationHandlerInstance().getDataVariable();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread loggerServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "loggerservice starterd ");
                while(true) {// TODO: 10/10/2016 change to CommunicationHandler.getCommunicationHandlerInstance().getRunningStatus())
                    try {
                        Thread.sleep(2000);
                        dataVariable.addToRpm(dataVariable.getRpm());
                        dataVariable.addToSpeed(dataVariable.getSpeed());
                        Log.e(TAG, "loggershits before average calculaton: " + dataVariable.getRpmList().size() + "<- rpms size " + dataVariable.getSpeedList().size() + "<- speeds size");
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


    public Double getTotalOfDoubleArray(ArrayList<Double> arrayToCount) {
        Double jeeben = 0.0;
        for (Double d : arrayToCount) {
            jeeben += d;
        }
        return jeeben;
    }

}
