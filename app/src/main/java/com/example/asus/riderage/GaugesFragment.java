package com.example.asus.riderage;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 28/09/2016.
 */

public class GaugesFragment extends Fragment implements View.OnClickListener {

    private Button startTrip, stopTrip;
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

        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void initButtonListners() {

        this.startTrip = (Button) fragmentView.findViewById(R.id.startTrip);
        this.stopTrip = (Button) fragmentView.findViewById(R.id.stopTrip);
        this.startTrip.setOnClickListener(this);
        this.stopTrip.setOnClickListener(this);
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
                    Log.e(TAG, "Started succesfully");
                } else {
                    Log.e(TAG, "Something failed in the checkSafeConnection");
                }
                break;
            case R.id.stopTrip:
                break;

        }
    }
}

