package com.example.asus.riderage;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 29/09/2016.
 */
public class AccelerometerMonitor implements Runnable,SensorEventListener{
    private static AccelerometerMonitor ourInstance = new AccelerometerMonitor();

    public static AccelerometerMonitor getInstance() {
        return ourInstance;
    }

    private AccelerometerMonitor() {
        Log.e(TAG, "AccelerometerMonitor: accel created" );
        //i like spaghetti
        ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).registerListener(this, ((SensorManager) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void run() {
        while (true){

        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        CommunicationHandler.getCommunicationHandlerInstance().setAccelInProgress(true);
        Log.e(TAG, "onSensorChanged: Acceleration " + event.values[0] + event.values[1] + event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
