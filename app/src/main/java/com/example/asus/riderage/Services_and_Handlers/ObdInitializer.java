package com.example.asus.riderage.Services_and_Handlers;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.asus.riderage.Bluetooth.BluetoothManagerClass;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Class used for initializing the OBD connection.
 * Init sequence is required when a new connection is made to wake up the ELM327
 */

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

    /**
     * Sends initialization commands to the OBD reader.
     * <p></p><b>Commands:</b></p>
     * <ul>
     *     <li>ObdResetCommand - resets the OBD</li>
     *     <li>EchoOffCommand - turns Echo off</li>
     *     <li>TimeoutCommand - defines how long the OBD waits for a reply from the ECU</li>
     *     <li>SelectProtocolCommand - defines the ECU protocol</li>
     * </ul>
     * @return
     */
    public boolean initializeObd() {
        try {
            // reset the ELM327
            new ObdResetCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //Log.e(TAG, "Thread sleep: error", e);
            }
            // init commands
            new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new TimeoutCommand(62).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            // set the bluetoothsocket back and start the ObdJobService
            this.bluetoothManagerInstance.setBluetoothSocket(this.bluetoothSocket);
            return true;
        } catch (IOException e) {
            try {
                bluetoothSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean call() throws Exception {
        return initializeObd();
    }
}