package com.example.asus.riderage;

import android.bluetooth.BluetoothSocket;
import android.telecom.Call;
import android.util.Log;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.concurrent.Callable;


// class used for initializing the OBD connection
// init sequence is required when a new connection is made to wake up the ELM327

public class ObdInitializer implements Callable<Boolean> {
    private static final String TAG = "ObdInitializer";
    BluetoothManagerClass bluetoothManagerInstance;
    private BluetoothSocket bluetoothSocket;
    private CommunicationHandler commHandler;

    public ObdInitializer() {
        bluetoothManagerInstance = BluetoothManagerClass.getBluetoothManagerClass();
        this.bluetoothSocket = this.bluetoothManagerInstance.getBluetoothSocket();
        this.commHandler = CommunicationHandler.getCommunicationHandlerInstance();
    }

    // https://www.elmelectronics.com/help/obd/tips/#327_Commands
    public boolean initializeObd() {
        try {
            Log.e(TAG, "InitOBD");
            // reset the ELM327
            new ObdResetCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            Log.e(TAG, "ObdResetComand was run");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Log.e(TAG, "Thread sleep: error", e);
            }
            // init commands
            Log.e(TAG, "Thread sleep done");
            new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new TimeoutCommand(62).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

            //RPMCommand make = new RPMCommand();
            //make.run(BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getInputStream(), BluetoothManagerClass.getBluetoothManagerClass().getBluetoothSocket().getOutputStream());
            //Log.e(TAG, "rpm : " + make.getFormattedResult());


            Log.e(TAG, "Init finished without errors ");
            Log.e(TAG, "Bluetooth socket connection " + bluetoothSocket.isConnected());
            // set the bluetoothsocket back and star the ObdJobService
            this.bluetoothManagerInstance.setBluetoothSocket(this.bluetoothSocket);
            /*commHandler.startObdJobService();*/
            return true;
        } catch (Exception e) {
            Log.e(TAG, "ERROR", e);
            try {
                Log.e(TAG, "Read return -1, closing BT socket");
                bluetoothSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }


    @Override
    public Boolean call() throws Exception {
        return initializeObd();
    }
}

