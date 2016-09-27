package com.example.asus.riderage;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Asus on 26/09/2016.
 */

public class BluetoothManagerClass {
    private static final String TAG = "BluetoothManagerClass";
    private static BluetoothManagerClass bluetoothManagerClass = new BluetoothManagerClass();
    private static BluetoothSocket bluetoothSocket;
    private Context context;

    private BluetoothManagerClass() { }

    public static BluetoothManagerClass getBluetoothManagerClass() {
        return bluetoothManagerClass;
    }

    protected void passContext(Context context) {
        this.context = context;

    }

    protected void initBluetooth() {
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

        // TODO: MOVE TO MAIN ACTIVITY

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.select_dialog_singlechoice, deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = devices.get(position).getAddress();
                BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                Thread connectionCreationThread = new Thread(new BluetoothConnection(uuid, device));
                connectionCreationThread.start();
            }
        });

        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();

    }

    protected BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    protected void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        Log.e(TAG, "setBluetoothSocket: Something is setting the BT socket, socket connection status is  " + bluetoothSocket.isConnected());
        BluetoothManagerClass.bluetoothSocket = bluetoothSocket;
    }
}
