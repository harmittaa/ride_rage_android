package com.example.asus.riderage;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private BluetoothManagerClass bluetoothManagerClass;
    private TextView dataTextView;
    private Button initObdButton, checkConnectionButton, startServiceButton, stopServiceButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);

        initObdButton = (Button) findViewById(R.id.initObdButton);
        checkConnectionButton = (Button) findViewById(R.id.checkConnectionButton);
        startServiceButton = (Button) findViewById(R.id.startServiceButton);
        stopServiceButton = (Button) findViewById(R.id.stopServiceBUtton);
        dataTextView = (TextView) findViewById(R.id.dataView);

        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.bluetoothManagerClass.passContext(this);
        this.bluetoothManagerClass.initBluetooth();
        initButtonListeners();
    }


    // creates the pop up for user to choose which BT device to use
    // also starts the thread that creates
/*
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
*/

/*
    public BluetoothSocket getBluetoothSocket() {
        Log.e(TAG, "getBluetoothSocket");
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        Log.e(TAG, "setBluetoothSocket: bluetoothSocket set");
        MainActivity.bluetoothSocket = bluetoothSocket;
    }
*/

    public void updateView(String stringToUpdate) {
        this.dataTextView.setText(stringToUpdate);
    }

    private void initButtonListeners() {
        this.initObdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread obdInitializerThread = new Thread(new ObdInitializer(MainActivity.this));
                obdInitializerThread.start();
            }
        });

        checkConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothSocket btS = bluetoothManagerClass.getBluetoothSocket();
                Log.e(TAG, "Bluetooth socket connection " + btS.isConnected());
            }
        });

        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Method to start the service
                startService(new Intent(getBaseContext(), ObdJobService.class));
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "onClick: STOPPING SERVICE");
                stopService(new Intent(getBaseContext(), ObdJobService.class));
            }
        });

    }

    /*private void initButtonListeners() {
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
                    aatc.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    Log.e(TAG, "CalculatedResult: " + aatc.getCalculatedResult());
                    Log.e(TAG, "FormattedResult " + aatc.getFormattedResult());

                    Log.e(TAG, "Init finished without errors ");
                    Log.e(TAG, "Bluetooth socket connection " + bluetoothSocket.isConnected());
                    Log.e(TAG, "Starting thread to get RPM");

                    Thread t = new Thread(new TestThread());
                    t.start();

                    *//*for (int i = 0; i < 6; i++) {*//*
                    while (true) {
                        try {
                            RPMCommand rpmCommand = new RPMCommand();
                            rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                            Log.e(TAG, "RPM formatted " + rpmCommand.getFormattedResult());
                            Log.e(TAG, "RPM calculated " + rpmCommand.getCalculatedResult());
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Thread sleep: error", e);
                        }
                        Thread.sleep(500);
                       // Log.e(TAG, "Loop done " + i);
                    }


*//*                    AirIntakeTemperatureCommand airIntakeTemperatureCommand = new AirIntakeTemperatureCommand();

                    BluetoothSocket btsocket = getBluetoothSocket();
                    Log.e(TAG, "bluetooth socket connection is " + btsocket.isConnected());
                    airIntakeTemperatureCommand.run(btsocket.getInputStream(), btsocket.getOutputStream());
                    Log.e(TAG, "airIntakeTemperatureCommand formatted" + airIntakeTemperatureCommand.getFormattedResult());
                    Log.e(TAG, "airIntakeTemperatureCommand calculated" + airIntakeTemperatureCommand.getCalculatedResult());*//*

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
                Thread t = new Thread(new TestThread());
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

        airIntakeTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "oilTempOnClick starting");
                AirIntakeTemperatureCommand airIntakeTemperatureCommand = new AirIntakeTemperatureCommand();
                try {
                    BluetoothSocket btsocket = getBluetoothSocket();
                    Log.e(TAG, "bluetooth socket connection is " + btsocket.isConnected());
                   // airIntakeTemperatureCommand.run(new ByteArrayInputStream("41 00 00 00>41 00 00 00>41 00 00 00>".getBytes()), new ByteArrayOutputStream());
                    airIntakeTemperatureCommand.run(btsocket.getInputStream(), btsocket.getOutputStream());
                    Log.e(TAG, "airIntakeTemperatureCommand formatted" + airIntakeTemperatureCommand.getFormattedResult());
                    Log.e(TAG, "airIntakeTemperatureCommand calculated" + airIntakeTemperatureCommand.getCalculatedResult());
                } catch (IOException e) {
                    Log.e(TAG, "onClick: OilTempError ", e);
                } catch (InterruptedException e) {
                    Log.e(TAG, "onClick: OilTempError ", e);
                }
            }
        });
    }


    private class TestThread implements Runnable {
        private BluetoothSocket btSocket;
        private int i = 0;
       // private RPMCommand rpmCommand;

        public TestThread() {
            this.btSocket = getBluetoothSocket();
        }

        @Override
        public void run() {
        //    rpmCommand = new RPMCommand();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(500);
                    i++;
                    Log.e(TAG, "bluetooth socket connection is " + btSocket.isConnected());
                    final RPMCommand rpmCommand = new RPMCommand();
                    rpmCommand.run(new ByteArrayInputStream("41 00 00 00>41 00 00 00>41 00 00 00>".getBytes()), new ByteArrayOutputStream());
//                    rpmCommand.run(this.btSocket.getInputStream(), this.btSocket.getOutputStream());
                    Log.e(TAG, "Round " + i + "RPM calculated " + rpmCommand.getCalculatedResult());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dataTextView.setText(rpmCommand.getCalculatedResult());
                        }
                    });

                } catch (IOException e) {
                    Log.e(TAG, "ERROR", e);
                } catch (InterruptedException e) {
                    Log.e("ERROR", "ERROR", e);
                }
            }
        }
    }

    private class AirIntakeTemperatureCommandThread implements Runnable {

        private BluetoothSocket btSocket;

        public AirIntakeTemperatureCommandThread() {
            this.btSocket = getBluetoothSocket();
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.e(TAG, "bluetooth socket connection is" + btSocket.isConnected());
                    AirIntakeTemperatureCommand airIntakeTemperatureCommand = new AirIntakeTemperatureCommand();
                    airIntakeTemperatureCommand.run(new ByteArrayInputStream("41 00 00 00>41 00 00 00>41 00 00 00>".getBytes()), new ByteArrayOutputStream());
                    //airIntakeTemperatureCommand.run(this.btSocket.getInputStream(), this.btSocket.getOutputStream());
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
    }*/

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }
}
