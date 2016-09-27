package com.example.asus.riderage;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;



public class ObdInitializer implements Runnable {
    private static final String TAG = "ObdInitializer";
    BluetoothManagerClass bluetoothManagerInstance;
    private BluetoothSocket bluetoothSocket;
    private MainActivity mainAct;

    public ObdInitializer(MainActivity mainAct) {
        bluetoothManagerInstance = BluetoothManagerClass.getBluetoothManagerClass();
        this.bluetoothSocket = this.bluetoothManagerInstance.getBluetoothSocket();
        this.mainAct = mainAct;
    }

    public void initializeObd() {
        try {
            Log.e(TAG, "InitOBD");
            new ObdResetCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            Log.d(TAG, "ObdResetComand was run");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread sleep: error", e);
            }
            Log.d(TAG, "Thread sleep done");
            new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new TimeoutCommand(62).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

            Log.e(TAG, "Init finished without errors ");
            Log.e(TAG, "Bluetooth socket connection " + bluetoothSocket.isConnected());
            this.bluetoothManagerInstance.setBluetoothSocket(this.bluetoothSocket);

/*
            Log.e(TAG, "Starting thread to get RPM");
            final RPMCommand rpmCommand = new RPMCommand();
            SpeedCommand speedCommand = new SpeedCommand();

            for (int i = 0; i < 100; i++) {
                try {
                    speedCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    Log.e(TAG, "RPM formatted " + rpmCommand.getFormattedResult());
                    Log.e(TAG, "Speed formatted " + speedCommand.getFormattedResult());

                    mainAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainAct.updateView(rpmCommand.getFormattedResult());
                        }
                    });

                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread sleep: error", e);
                }
                Thread.sleep(500);
            }
*/

/*            AirIntakeTemperatureCommand airIntakeTemperatureCommand = new AirIntakeTemperatureCommand();
            Log.e(TAG, "bluetooth socket connection is " + this.bluetoothSocket.isConnected());
            airIntakeTemperatureCommand.run(this.bluetoothSocket.getInputStream(), this.bluetoothSocket.getOutputStream());
            Log.e(TAG, "airIntakeTemperatureCommand formatted" + airIntakeTemperatureCommand.getFormattedResult());
            Log.e(TAG, "airIntakeTemperatureCommand calculated" + airIntakeTemperatureCommand.getCalculatedResult());*/

        } catch (IOException e) {
            Log.e(TAG, "ERROR", e);
            try {
                Log.e(TAG, "Read return -1, closing BT socket");
                bluetoothSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "ERROR", e);
        }
    }

    @Override
    public void run() {
        initializeObd();
    }
}

