package com.example.asus.riderage;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    ImageButton blSelectBtn;
    BluetoothSocket bluetoothSocket;
    SpeedometerGauge speedoRPM, speedoSpeed;
    ArrayList deviceStrs;
    ArrayList<BluetoothDevice> devices;
    BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        initBluetooth();
        initButtonListners();
        initSpeedos();

    }

    private void initBluetooth() {
        Log.e(TAG, "InitBL starting");
        this.deviceStrs = new ArrayList();
        devices = new ArrayList();
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }
        }

        // show list
        /*
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = devices.get(position).getAddress();
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                ArrayList<UUID> jeeben = new ArrayList<UUID>();
                //for(ParcelUuid u:devices.get(position).getUuids()){

                //}
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                Thread t = new Thread(new ConnectRunnable(uuid, device));
                t.start();

            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
        */

    }

    private void initButtonListners() {
        this.blSelectBtn = (ImageButton) findViewById(R.id.selectDeviceButton);
        this.blSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!MainActivity.this.btAdapter.isEnabled()){
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, RESULT_OK);
                    showDeviceSelectScreen();
                }else showDeviceSelectScreen();
            }
        });

        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    AmbientAirTemperatureCommand aa = new AmbientAirTemperatureCommand();
                    RPMCommand rpm = new RPMCommand();
                    rpm.run(bluetoothSocket.getInputStream(),bluetoothSocket.getOutputStream());
                    //aa.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    Log.e(TAG, "CalculatedResult: " + rpm.getCalculatedResult());
                    Log.e(TAG, "FormattedResult " + rpm.getFormattedResult());

                    //new RPMCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
/*
                    Log.e(TAG, "Air temp " + new AmbientAirTemperatureCommand().getCalculatedResult());
                    Log.e(TAG, "Module voltage " + new ModuleVoltageCommand().getFormattedResult());

                    Log.e(TAG, "Init finished without errors ");
                    Log.e(TAG, "Bluetooth socket connection " + bluetoothSocket.isConnected());
                } catch (IOException e) {
                    Log.e(TAG, "ERROR", e);
                } catch (InterruptedException e) {
                    Log.e(TAG, "ERROR", e);
                }
            }
        });*/

    }

    private void showDeviceSelectScreen() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = devices.get(position).getAddress();
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                ArrayList<UUID> jeeben = new ArrayList<UUID>();
                //for(ParcelUuid u:devices.get(position).getUuids()){

                //}
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                Thread t = new Thread(new ConnectRunnable(uuid, device));
                t.start();

            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }

    private void initSpeedos() {
        speedoRPM = (SpeedometerGauge) findViewById(R.id.speedoRPM);

        speedoRPM.setMaxSpeed(60);
        speedoRPM.setMajorTickStep(10);
        speedoRPM.setMinorTicks(4);

        speedoRPM.addColoredRange(0, 22, Color.GREEN);
        speedoRPM.addColoredRange(22, 32, Color.YELLOW);
        speedoRPM.addColoredRange(32, 60, Color.RED);

        speedoRPM.setLabelTextSize(40);

        speedoRPM.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        speedoSpeed = (SpeedometerGauge) findViewById(R.id.speedoSpeed);
        speedoSpeed.setMaxSpeed(240);
        speedoSpeed.setMajorTickStep(20);
        speedoSpeed.setMinorTicks(1);

        speedoSpeed.setLabelTextSize(20);

        speedoSpeed.setLabelConverter(new SpeedometerGauge.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });


    }

/*    private void sendCommand() {
        String airTempCommand = "01 46";
        bluetoothSocket.getOutputStream().write(airTempCommand).get;

    }*/

    private class TestThread implements Runnable {

        @Override
        public void run() {
            //   RPMCommand rpmCommand = new RPMCommand();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    //       rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    //      String theString = IOUtils.toString(bluetoothSocket.getInputStream(), "UTF-8");
                    //       Log.e(TAG, "THESTRING" + theString);
                    new AmbientAirTemperatureCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());

                } catch (IOException e) {
                    Log.e(TAG, "ERROR", e);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "ERROR", e);
                }
                // TODO handle commands result
                //Log.e(TAG, "RPM: " + rpmCommand.getFormattedResult());
                //Log.e(TAG, "Speed: " + rpmCommand.getFormattedResult());

            }

        }

    }

    private class ConnectRunnable implements Runnable {
        private UUID uuidToConnect;
        BluetoothDevice device;

        public ConnectRunnable(UUID uid, BluetoothDevice dev) {
            this.uuidToConnect = uid;
            this.device = dev;
        }

        @Override
        public void run() {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuidToConnect);
                Log.e(TAG, "Connecting with " + uuidToConnect.toString());
                bluetoothSocket.connect();
                Log.e(TAG, "Connected with " + uuidToConnect.toString());
            } catch (IOException e) {
                Log.e(TAG, "ERROR", e);
                try {
                    Log.e(TAG, "trying fallback...");

                    bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                    Log.e(TAG, "socket creatd");
                    bluetoothSocket.connect();

                    Log.e(TAG, "Connected");
                } catch (Exception e2) {
                    Log.e("", "Couldn't establish Bluetooth connection!", e2);
                }
            }
        }
    }

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

}
