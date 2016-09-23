package com.example.asus.riderage;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.commands.ObdMultiCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.ObdWarmstartCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    TextView text1;
    Button button, dataButton, checkConnectionButton, airIntakeButton;
    BluetoothSocket bluetoothSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        initBluetooth();
        button = (Button) findViewById(R.id.testButton);
        dataButton = (Button) findViewById(R.id.getDataButton);
        airIntakeButton = (Button) findViewById(R.id.airIntakeButton);
        checkConnectionButton = (Button) findViewById(R.id.checkConnectionButton);
        text1 = (TextView) findViewById(R.id.dataView);
        initButtonListners();
    }

    private void initBluetooth() {
        Log.e(TAG, "InitBL starting");
        ArrayList deviceStrs = new ArrayList();
        final ArrayList<BluetoothDevice> devices = new ArrayList();

        final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceStrs.add(device.getName() + "\n" + device.getAddress());
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
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                Thread t = new Thread(new ConnectRunnable(uuid, device));
                t.start();
            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();

    }

    private void initButtonListners() {
        button.setOnClickListener(new View.OnClickListener() {
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

                    AmbientAirTemperatureCommand aatc = new AmbientAirTemperatureCommand();
                    aatc.run(bluetoothSocket.getInputStream(),bluetoothSocket.getOutputStream());
                    Log.e(TAG, "CalculatedResult: " + aatc.getCalculatedResult());
                    Log.e(TAG, "FormattedResult " + aatc.getFormattedResult());

                    Log.e(TAG, "Init finished without errors ");
                    Log.e(TAG, "Bluetooth socket connection " + bluetoothSocket.isConnected());


                    Log.e(TAG, "Starting thread to get RPM");

                    for (int i = 0; i < 20; i++) {
                        try {
                            RPMCommand rpmCommand = new RPMCommand();
                            rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                            Log.e(TAG, "RPM formatted " + rpmCommand.getFormattedResult());
                            Log.e(TAG, "RPM calculated " + rpmCommand.getCalculatedResult());
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Thread sleep: error", e);
                        }
                        Thread.sleep(500);
                        Log.e(TAG, "Loop done " + i);
                    }


                   /* Thread t = new Thread(new TestThread(bluetoothSocket));
                    t.start();*/

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
        });

        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Starting thread to get RPM");
                Thread t = new Thread(new TestThread(bluetoothSocket));
                t.start();
            }
        });

        checkConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Bluetooth socket connection " + bluetoothSocket.isConnected());
            }
        });

        airIntakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Starting airIntakeThread");
                Thread airIntakeThread = new Thread(new AirIntakeTemperatureCommandThread());
                airIntakeThread.start();
            }
        });
    }


    private class TestThread implements Runnable {
        BluetoothSocket bluetoothSocket;

        public TestThread(BluetoothSocket bluetoothSocket) {
            this.bluetoothSocket = bluetoothSocket;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.d(TAG, "ObdResetComand was run");
                    RPMCommand rpmCommand = new RPMCommand();
                    rpmCommand.run(this.bluetoothSocket.getInputStream(), this.bluetoothSocket.getOutputStream());
                    Log.e(TAG, "RPM formatted " + rpmCommand.getFormattedResult());
                    Log.e(TAG, "RPM calculated " + rpmCommand.getCalculatedResult());
                } catch (IOException e) {
                    Log.e(TAG, "ERROR", e);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "ERROR", e);
                }
            }
        }
    }

    private class AirIntakeTemperatureCommandThread implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    AirIntakeTemperatureCommand airIntakeTemperatureCommand = new AirIntakeTemperatureCommand();
                    airIntakeTemperatureCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    Log.e(TAG, "airIntakeTemperatureCommand formatted " + airIntakeTemperatureCommand.getFormattedResult());
                    Log.e(TAG, "airIntakeTemperatureCommand calculated " + airIntakeTemperatureCommand.getCalculatedResult());
                } catch (IOException e) {
                    Log.e(TAG, "AirIntakeThread FAIL");
                    Log.e(TAG, "ERROR", e);
                } catch (InterruptedException e) {
                    Log.e(TAG, "AirIntakeThread FAIL");
                    Log.e("ERROR", "ERROR", e);
                }
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
                    Log.e(TAG,"Trying fallback socket");
                    bluetoothSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                    Log.e(TAG,"Fallback socket created, trying to connect...");
                    bluetoothSocket.connect();
                    Log.e(TAG,"Connection established");
                }
                catch (Exception e2) {
                    Log.e(TAG, "Couldn't establish Bluetooth connection!", e2);
                }
            }
        }
    }

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    private void checkBLE(){

    }
}
