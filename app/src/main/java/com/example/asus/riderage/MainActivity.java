package com.example.asus.riderage;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.enums.ObdProtocols;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    TextView text1;
    Button button, dataButton;
    BluetoothSocket bluetoothSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        initBluetooth();
        button = (Button) findViewById(R.id.testButton);
        dataButton = (Button) findViewById(R.id.getDataButton);
        text1 = (TextView)findViewById(R.id.dataView);
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
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                ArrayList<UUID> jeeben = new ArrayList<UUID>();
                for(ParcelUuid u:devices.get(position).getUuids()){
                    UUID uuid = UUID.fromString(u.toString());
                    Thread t = new Thread(new ConnectRunnable(uuid,device));
                    t.start();
                }
            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();

    }

    private void initButtonListners(){
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.e(TAG,"InitOBD");
                    new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new TimeoutCommand(125).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                } catch (IOException e) {
                    Log.e(TAG,"ERROR",e);
                } catch (InterruptedException e) {
                    Log.e(TAG,"ERROR",e);
                }
            }
        });

        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread(new TestThread());
                t.start();
            }
        });
    }

    private class TestThread implements Runnable {

        @Override
        public void run() {
            RPMCommand rpmCommand = new RPMCommand();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    new AmbientAirTemperatureCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    rpmCommand.run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    String theString = IOUtils.toString(bluetoothSocket.getInputStream(), "UTF-8");
                    Log.e(TAG,"THESTRING" + theString);
                } catch (IOException e) {
                    Log.e(TAG,"ERROR",e);
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
        public ConnectRunnable(UUID uid,BluetoothDevice dev){
            this.uuidToConnect  = uid;
            this.device = dev;
        }

        @Override
        public void run() {
            try {
                try {
                    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                    if(!bluetooth.isEnabled()){
                        bluetooth.enable();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuidToConnect);
                Log.e(TAG,"Connecting with" + uuidToConnect.toString());
                Thread.sleep(500);
                if(!bluetoothSocket.isConnected())
                    bluetoothSocket.connect();
                Log.e(TAG,"Connected with" + uuidToConnect.toString());
                try {
                    Log.e(TAG,"InitOBD");
                    new EchoOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new LineFeedOffCommand().run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new TimeoutCommand(125).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                    new SelectProtocolCommand(ObdProtocols.AUTO).run(bluetoothSocket.getInputStream(), bluetoothSocket.getOutputStream());
                } catch (IOException e) {
                    Log.e(TAG,"ERROR",e);
                } catch (InterruptedException e) {
                    Log.e(TAG,"ERROR",e);
                }
            } catch (IOException e) {
                Log.e(TAG,"ERROR",e);
            } catch (InterruptedException e) {
                Log.e(TAG,"ERROR",e);
            }
        }
    }

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }
}
