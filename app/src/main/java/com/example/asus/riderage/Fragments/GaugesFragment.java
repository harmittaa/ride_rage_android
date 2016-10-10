package com.example.asus.riderage.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.MainActivity;
import com.example.asus.riderage.R;
import com.example.asus.riderage.Misc.UpdatableFragment;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Handles gauges
 */


public class GaugesFragment extends Fragment implements View.OnClickListener, UpdatableFragment {

    private Button startTrip, stopTrip, tripsListButton;
    ImageButton blSelectBtn;
    SpeedometerGauge speedoRPM, speedoSpeed;
    ArrayList<BluetoothDevice> devices;
    BluetoothAdapter btAdapter;
    TextView accelTest;
    private View fragmentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_gauges, container, false);
        accelTest = (TextView) fragmentView.findViewById(R.id.accelTest);
        initButtonListners();
        initSpeedos();
        updateOnStateChanged(CommunicationHandler.getCommunicationHandlerInstance().getConnection_state());
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getMainActivity().changeActionBarIcons(Constants.FRAGMENT_TYPES.GAUGES_FRAGMENT);
    }

    private void initButtonListners() {

        this.startTrip = (Button) fragmentView.findViewById(R.id.startTrip);
        this.stopTrip = (Button) fragmentView.findViewById(R.id.stopTrip);
        this.tripsListButton = (Button) fragmentView.findViewById(R.id.listFragmentButton);
        this.startTrip.setOnClickListener(this);
        this.stopTrip.setOnClickListener(this);
        this.tripsListButton.setOnClickListener(this);
    }


    private void initSpeedos() {
        speedoRPM = (SpeedometerGauge) fragmentView.findViewById(R.id.speedoRPM);

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

        speedoSpeed = (SpeedometerGauge) fragmentView.findViewById(R.id.speedoSpeed);
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

    @Override
    public void onResume() {
        super.onResume();
        setupBackButtonActon();
        getMainActivity().changeActionBarIcons(Constants.FRAGMENT_TYPES.GAUGES_FRAGMENT);
    }

    private void setupBackButtonActon() {
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(
                new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            if (event.getAction()!=KeyEvent.ACTION_DOWN)
                                return true;
                            Log.e(TAG, "onKey: BACK PRESSED");
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(R.string.quit_confirmation)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            getMainActivity().finishAffinity();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User cancelled the dialog
                                        }
                                    })
                                    .create()
                                    .show();

                        }
                        return true;
                    }
                }

        );
    }


    public void updateGauges(final double rpm, final double speed) {
        speedoRPM.setSpeed((rpm / 100), 0, 0);
        speedoSpeed.setSpeed(speed, 0, 0);
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startTrip:
                if (CommunicationHandler.getCommunicationHandlerInstance().checkSafeConnection()) {
                    //Log.e(TAG, "Started succesfully");
                } else {
                    //Log.e(TAG, "Something failed in the checkSafeConnection");
                }
                break;
            case R.id.stopTrip:
                //TODO 1. send obd close command through obdjobservice 2. close bluetooth socket
                CommunicationHandler.getCommunicationHandlerInstance().getCurrentTripHandler().stopCurrentTrip();
                //getMainActivity().changeVisibleFragmentType(Constants.FRAGMENT_TYPES.RESULT_FRAGMENT);
                break;
            case R.id.listFragmentButton:
                getMainActivity().changeVisibleFragmentType(Constants.FRAGMENT_TYPES.TRIPS_LIST_FRAGMENT);
                break;

        }
    }

    // Requires runOnUiThread because GaugesFragment didn't create the view hierarchy
    @Override
    public void updateOnStateChanged(final Constants.CONNECTION_STATE connection_state) {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*switch (connection_state) {
                    case CONNECTED_NOT_RUNNING:
                        startTrip.setVisibility(View.VISIBLE);
                        stopTrip.setVisibility(View.GONE);
                        break;
                    case CONNECTED_RUNNING:
                        startTrip.setVisibility(View.GONE);
                        stopTrip.setVisibility(View.VISIBLE);
                        break;
                    case DISCONNECTED:
                        startTrip.setVisibility(View.GONE);
                        stopTrip.setVisibility(View.GONE);
                        break;
                }*/
            }
        });
    }
}

