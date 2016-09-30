package com.example.asus.riderage;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.example.asus.riderage.Database.TripDatabaseHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private BluetoothManagerClass bluetoothManagerClass;
    private CommunicationHandler communicationHandler;
    private TripDatabaseHelper tripDbHelper;
    private final String TAG = "MainActivity";
    TextView accelTest;
    GaugesFragment gaugeFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        accelTest = (TextView)findViewById(R.id.accelTest);
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler.passContext(this);
        }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        gaugeFragment = new GaugesFragment();
        fragmentTransaction.add(R.id.replaceWithFragment, gaugeFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                showDeviceSelectScreen();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void activateDeviceSelectScreen(){
        if (!communicationHandler.checkBluetoothStatus(true)) {

        } else showDeviceSelectScreen();
    }


    private void showDeviceSelectScreen() {

        ArrayList<String> deviceStrs = this.communicationHandler.getDeviceStrings();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(communicationHandler.createBluetoothConnection(which))
                    Log.e(TAG, "onClick: success" );
                else Log.e(TAG, "onClick: failure");
            }
        });
        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }



    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    public void makeToast(final String stringToShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), stringToShow, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public void startObdJobService() {
        startService(new Intent(this, ObdJobService.class));
    }

    public void stopObdJobService() {
        stopService(new Intent(this, ObdJobService.class));
    }

    public void updateGauges(final double rpm, final double speed) {

        //TODO change to call fragment method
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gaugeFragment.updateGauges(rpm, speed);
            }
        });
    }

}


