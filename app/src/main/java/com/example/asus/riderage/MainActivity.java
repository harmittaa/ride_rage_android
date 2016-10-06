package com.example.asus.riderage;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.riderage.Bluetooth.BluetoothManagerClass;
import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.Fragments.GaugesFragment;
import com.example.asus.riderage.Fragments.ResultFragment;
import com.example.asus.riderage.Fragments.TripsListView;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Misc.UpdatableFragment;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;
import com.example.asus.riderage.Services_and_Handlers.ObdJobService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private BluetoothManagerClass bluetoothManagerClass;
    private CommunicationHandler communicationHandler;
    private TripDatabaseHelper tripDbHelper;
    private final String TAG = "MainActivity";
    private UpdatableFragment currentFragment;
    private Constants.FRAGMENT_TYPES currentFragmentType;
    TextView accelTest;
    private GaugesFragment gaugeFragment;
    private ResultFragment resultFragment;
    private TripsListView tripsListFragment;
    Menu menu;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_main, menu);
        this.menu = menu;
        /*
         * Change visible fragment here, because menu is inflated after onresume, so menu will be null
         */
        changeVisibleFragmentType(Constants.FRAGMENT_TYPES.GAUGES_FRAGMENT);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
        setContentView(R.layout.activity_main);
        accelTest = (TextView) findViewById(R.id.accelTest);
        gaugeFragment = new GaugesFragment();
        resultFragment = new ResultFragment();
        tripsListFragment = new TripsListView();
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        this.bluetoothManagerClass = BluetoothManagerClass.getBluetoothManagerClass();
        this.communicationHandler.passContext(this);
        checkGpsStatus();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void changeVisibleFragmentType(Constants.FRAGMENT_TYPES fragment_type) {
        this.currentFragmentType = fragment_type;
        changeVisibleFragment();
    }

    private void changeVisibleFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (this.currentFragmentType) {
            case GAUGES_FRAGMENT:
                this.currentFragment = this.gaugeFragment;
                fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment);
                fragmentTransaction.commit();
                changeActionBarIcons(this.currentFragmentType);
                break;
            case RESULT_FRAGMENT:
                this.currentFragment = this.resultFragment;
                fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment).addToBackStack("jeeben");
                fragmentTransaction.commit();
                changeActionBarIcons(this.currentFragmentType);
                break;
            case TRIPS_LIST_FRAGMENT:
                this.currentFragment = this.tripsListFragment;
                fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment).addToBackStack("jeeben");
                fragmentTransaction.commit();
                changeActionBarIcons(this.currentFragmentType);
                break;

        }
    }

    private void changeActionBarIcons(Constants.FRAGMENT_TYPES fragType){
        /*switch (this.currentFragmentType) {
            case GAUGES_FRAGMENT:
                menu.findItem(R.id.action_bluetooth).setVisible(true);
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_edit).setVisible(false);
                break;
            case RESULT_FRAGMENT:
                menu.findItem(R.id.action_bluetooth).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(true);
                menu.findItem(R.id.action_edit).setVisible(true);
                break;
        }*/
    }

    public void setFragmentTripId(long tripId) {
        this.resultFragment.setTripId(tripId);
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

    public void activateDeviceSelectScreen() {
        if (!communicationHandler.checkBluetoothStatus(true)) {

        } else showDeviceSelectScreen();
    }

    private void showDeviceSelectScreen() {

        ArrayList<String> deviceStrs = this.communicationHandler.getDeviceStrings();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.e(TAG, "onClick: aids is go");
                ConnectToOBDTask aidsTask = new ConnectToOBDTask(which);
                aidsTask.execute();
                dialog.dismiss();
            }
        });
        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: Main activity destroyed");
        stopService(new Intent(this, ObdJobService.class));
        BluetoothManagerClass.getBluetoothManagerClass().closeSocket();
        super.onDestroy();
    }

    private void requestPermission() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }

    public void makeToast(final String stringToShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), stringToShow, Toast.LENGTH_SHORT);
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

    public void updateOnConnectionStateChanged(Constants.CONNECTION_STATE newConnectionState) {
        this.currentFragment.updateOnStateChanged(newConnectionState);
    }

    private void checkGpsStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            Log.e(TAG, "Access");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            Log.e(TAG, "Access");
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        Log.e(TAG, "Permission " + permissionCheck);

    }


    private class ConnectToOBDTask extends AsyncTask<Integer, Long, Boolean> {
        ProgressDialog dialog;
        int which;

        public ConnectToOBDTask(int which) {
            this.which = which;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this, "",
                    "Connecting. Please wait...", true);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (BluetoothManagerClass.getBluetoothManagerClass().createBluetoothConnection(this.which)) {
                Log.e(TAG, "onClick: success");
                return true;
            } else {
                Log.e(TAG, "onClick: failure");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            //TODO show either connection complete or failed, dismiss popup
            dialog.dismiss();
            if (aBoolean) {
                makeToast("Connection Succesful");
                CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.CONNECTED_NOT_RUNNING);
            } else {
                makeToast("Connection Failed");
                CommunicationHandler.getCommunicationHandlerInstance().setConnection_state(Constants.CONNECTION_STATE.DISCONNECTED);
            }
        }
    }
}


