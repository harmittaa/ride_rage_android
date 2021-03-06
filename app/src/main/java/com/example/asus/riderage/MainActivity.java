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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.riderage.Bluetooth.BluetoothManagerClass;
import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.Fragments.GaugesFragment;
import com.example.asus.riderage.Fragments.ResultFragment;
import com.example.asus.riderage.Fragments.TripsListFragment;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Misc.UpdatableFragment;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;
import com.example.asus.riderage.Services_and_Handlers.LoggerService;
import com.example.asus.riderage.Services_and_Handlers.ObdJobService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private CommunicationHandler communicationHandler;
    private TripDatabaseHelper tripDbHelper;
    private final String TAG = "MainActivity";
    private UpdatableFragment currentFragment;
    private Constants.FRAGMENT_TYPES currentFragmentType;
    TextView accelTest;
    private GaugesFragment gaugeFragment;
    private ResultFragment resultFragment;
    Menu menu;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_main, menu);
        this.menu = menu;
        /*
         * Change visible fragment here, because menu is inflated after onresume, so menu will be null
         */
        changeVisibleFragmentType(Constants.FRAGMENT_TYPES.GAUGES_FRAGMENT, false);
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
        this.communicationHandler = CommunicationHandler.getCommunicationHandlerInstance();
        this.communicationHandler.passContext(this);
        checkGpsStatus();
        TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);
        dbHelper.getTotalDistanceDriven();
        //dbHelper.exportDatabase();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void changeVisibleFragmentType(Constants.FRAGMENT_TYPES fragment_type, boolean withBackStack) {
        this.currentFragmentType = fragment_type;
        changeVisibleFragment(withBackStack);
    }

    private void changeVisibleFragment(boolean withBackStack) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.slide_left, R.animator.slide_right, R.animator.slide_left_back, R.animator.slide_right_back);
        switch (this.currentFragmentType) {
            case GAUGES_FRAGMENT:
                this.currentFragment = this.gaugeFragment = new GaugesFragment();
                fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment).addToBackStack("huuben");
                fragmentTransaction.commit();
                break;
            case RESULT_FRAGMENT:
                this.currentFragment = this.resultFragment = new ResultFragment();
                if (withBackStack)
                    fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment).addToBackStack("huuben");
                else
                    fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment);
                fragmentTransaction.commit();
                break;
            case TRIPS_LIST_FRAGMENT:
                this.currentFragment = new TripsListFragment();
                if (withBackStack)
                    fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment).addToBackStack("huuben");
                else
                    fragmentTransaction.replace(R.id.replaceWithFragment, (Fragment) this.currentFragment);
                fragmentTransaction.commit();
                break;

        }
    }

    public void resetActionBarTitle(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setSubtitle(null);
            }
        });
    }

    public void changeActionBarIcons(Constants.FRAGMENT_TYPES fragType) {
        switch (fragType) {
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
            case TRIPS_LIST_FRAGMENT:
                menu.findItem(R.id.action_bluetooth).setVisible(false);
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_edit).setVisible(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, "onOptionsItemSelected: clicked menu btton: " + item.getItemId());
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                showDeviceSelectScreen();
                return true;
            case R.id.action_delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.confirmation_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                tripDbHelper = new TripDatabaseHelper(MainActivity.this);
                                tripDbHelper.deleteTrip(communicationHandler.getTripId());
                                //changeVisibleFragmentType(Constants.FRAGMENT_TYPES.TRIPS_LIST_FRAGMENT, false);
                                onBackPressed();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .create()
                        .show();
                return true;
            case R.id.action_edit:
                AlertDialog.Builder builderi = new AlertDialog.Builder(this);
                View alertView = CommunicationHandler.getCommunicationHandlerInstance().getContext().getLayoutInflater().inflate(R.layout.dialog_textinput, null);
                builderi.setView(alertView);
                final EditText newText = (EditText) alertView.findViewById(R.id.dialog_textInput);
                builderi.setMessage(R.string.enter_name_for_trip)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String editedName = newText.getText().toString();
                                if (!TextUtils.isEmpty(editedName)) {
                                    tripDbHelper = new TripDatabaseHelper(MainActivity.this);
                                    tripDbHelper.editTripName(editedName, CommunicationHandler.getCommunicationHandlerInstance().getTripId());
                                    // TODO: 12/10/2016 change action bar subtitle
                                    getSupportActionBar().setSubtitle(editedName);
                                }
                                else
                                    makeToast("Name was not changed");
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        })
                        .create()
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeviceSelectScreen() {

        ArrayList<String> deviceStrs = this.communicationHandler.getDeviceStrings();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

    public void stopLoggerService() {
        stopService(new Intent(this, LoggerService.class));
    }

    public void updateGauges(final double rpm, final double speed) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gaugeFragment.updateGauges(rpm, speed);
            }
        });
    }

    public void updateDistanceTextView(final double newTotalDistance){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gaugeFragment.updateDistance(round(newTotalDistance,1));
            }
        });
    }

    /**
     * Rounds given double to given decimals
     * @param value Double to round
     * @param places Amount of decimals to be rounded to
     * @return Rounded double
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void updateOnConnectionStateChanged(Constants.CONNECTION_STATE newConnectionState) {
        this.currentFragment.updateOnStateChanged(newConnectionState);
    }

    private void checkGpsStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            //Log.e(TAG, "Access");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            //Log.e(TAG, "Access");
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        //Log.e(TAG, "Permission " + permissionCheck);

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
            dialog.dismiss();
            if (aBoolean) {
                makeToast("Connection Succesful");
                CommunicationHandler.getCommunicationHandlerInstance().setConnectionState(Constants.CONNECTION_STATE.CONNECTED_NOT_RUNNING);
            } else {
                makeToast("Connection Failed");
                CommunicationHandler.getCommunicationHandlerInstance().setConnectionState(Constants.CONNECTION_STATE.DISCONNECTED);
            }
        }
    }
}


