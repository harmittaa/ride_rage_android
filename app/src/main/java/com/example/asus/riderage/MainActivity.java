package com.example.asus.riderage;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    Button button, dataButton;
    BluetoothSocket bluetoothSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBluetooth();
        button = (Button) findViewById(R.id.testButton);
        dataButton = (Button) findViewById(R.id.getDataButton);
        button.setOnClickListener(buttonListener);
        dataButton.setOnClickListener(dataButtonListener);
    }

    private View.OnClickListener dataButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Thread t = new Thread(new TestThread());
            t.start();
        }
    };

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                new TimeoutCommand(125).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    private void initBluetooth() {

        ArrayList deviceStrs = new ArrayList();
        final ArrayList<BluetoothDevice> devices = new ArrayList();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                //Log.e(TAG, "name " + device.getName() + "UUIDs " + device.getUuids()[0] + " size " + device.getUuids().length + " address " + device.getAddress());
               // devices.add(device.getAddress());
                devices.add(device);
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = devices.get(position).getAddress();
                // TODO save deviceAddress
                Log.e(TAG, "Device address " + deviceAddress);
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                UUID uuid = UUID.fromString(devices.get(position).getUuids()[0].toString());
                try {
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    bluetoothSocket.connect();
                } catch (IOException e) {
                    Log.e(TAG, "Exception creating RfcommSocket", e);
                }
            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();

    }

    private class TestThread implements Runnable {

        @Override
        public void run() {
            RPMCommand rpmCommand = new RPMCommand();
            while (!Thread.currentThread().isInterrupted())
            {
                try {
                    rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // TODO handle commands result
                Log.e(TAG, "RPM: " + rpmCommand.getFormattedResult());
                Log.e(TAG, "Speed: " + rpmCommand.getFormattedResult());
            }

        }


    }
}
