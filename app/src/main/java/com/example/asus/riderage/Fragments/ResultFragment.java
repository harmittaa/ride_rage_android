package com.example.asus.riderage.Fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.MainActivity;
import com.example.asus.riderage.R;
import com.example.asus.riderage.Misc.UpdatableFragment;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/*

Handles the UI of the result screen, class includes AsyncTask for parsing data from SQLite

*/

public class ResultFragment extends Fragment implements UpdatableFragment, OnMapReadyCallback {
    private final String TAG = "ResultFragment";
    private View fragmentView;
    private TextView durationTextView, distanceTextView, avgSpeedTextView, avgRpmTextView, textView;
    private Constants.FRAGMENT_CALLER fragment_caller;
    private static long tripId;
    private GoogleMap googleMap;
    private MapFragment mapFragment;
    ArrayList<PolylineOptions> polylineOptionsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setTripId(CommunicationHandler.getCommunicationHandlerInstance().getTripId());
        fragmentView = inflater.inflate(R.layout.fragment_result, container, false);
        textView = (TextView) fragmentView.findViewById(R.id.placeHolderResultLabel);
        textView.setText("It works");
        mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        durationTextView = (TextView) fragmentView.findViewById(R.id.durationResultLabel);
        distanceTextView = (TextView) fragmentView.findViewById(R.id.distanceResultLabel);
        avgSpeedTextView = (TextView) fragmentView.findViewById(R.id.avgSpeedResultLabel);
        avgRpmTextView = (TextView) fragmentView.findViewById(R.id.avgRpmResultLabel);

        return fragmentView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Log.e(TAG, "onMapReady: maps ready to use");
        this.googleMap = googleMap;
        DataFetcher dataFetcher = new DataFetcher(tripId);
        dataFetcher.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate result fragment: 6." );
    }

    public void updateFragmentView(final String duration, final String distance, final String avgSpeed, final String avgRpm, final String placeHolder) {
        Log.e(TAG, "endTrip params:\ntripid "+tripId+"\ndistance " + distance + "\nduration " + duration + "\naveragespeed " + avgSpeed + "\naveragerpm " + avgRpm + "\nconsumption " + placeHolder);
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ResultFragment.this.durationTextView.setText(duration);
                ResultFragment.this.distanceTextView.setText(distance);
                ResultFragment.this.avgSpeedTextView.setText(avgSpeed);
                ResultFragment.this.avgRpmTextView.setText(avgRpm);
                ResultFragment.this.textView.setText(placeHolder);
            }
        });
    }

    // calls runonuithread to draw the polylines on the map
    private void drawLinesOnMap() {
        //Log.e(TAG, "drawLinesOnMap: adding polylines, size " + polylineOptionsList.size());
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (PolylineOptions po : polylineOptionsList) {
                    //Log.e(TAG, "run: color of polyline " + po.getColor());
                    googleMap.addPolyline(po);
                }
            }
        });
    }

    @Override
    public void updateOnStateChanged(Constants.CONNECTION_STATE connection_state) {
    }

    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    public Constants.FRAGMENT_CALLER getFragment_caller() {
        return fragment_caller;
    }

    public void setFragment_caller(Constants.FRAGMENT_CALLER fragment_caller) {
        this.fragment_caller = fragment_caller;
    }

    public static long getTripId() {
        return tripId;
    }


    public static void setTripId(long tripId) {
        ResultFragment.tripId = tripId;
    }

    /* Inner class for fetching data from the DB */
    private class DataFetcher extends AsyncTask<Integer, Long, Boolean> {
        private long tripId;
        private TripDatabaseHelper dbHelper;
        double prevRPM = 0;
        LatLng prevLatLng;
        PolylineOptions currentPolylineOption = new PolylineOptions();

        // constructor to pass tripId
        public DataFetcher(long tripId) {
            this.tripId = tripId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show pop up here
        }

        // gets trip data first, then gets datapoints
        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.e(TAG, "doInBackground: 7." );
            this.dbHelper = new TripDatabaseHelper(getContext());
            Cursor cursor = this.dbHelper.getFullTripData(getTripId());
            cursor.moveToFirst();
            ////Log.e(TAG, "doInBackground: num of stufs:" + cursor.getCount() + "\n Trip Id: " + this.tripId);
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_DURATION_MS));
            String distance = ((Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_DISTANCE))))/1000) + "KM";
            //Log.e(TAG, "doInBackground: shittershow" + cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_RPM))  );
            String avgSpd = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_SPEED)) + "KM/H";
            ////Log.e(TAG, "doInBackground: average RPM is " + cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_RPM));
            String avgrpm = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_RPM)) + "RPM";
            //Log.e(TAG, "doInBackground: SHITSHOW:\n" + duration + "\n" + distance + "\n" + avgSpd + "\n" + avgrpm);
            updateFragmentView(TimeUnit.MILLISECONDS.toSeconds(Integer.parseInt(duration))+"Sec", distance, avgSpd, avgrpm, "jeeben");
            getDataPoints();
            return null;
        }

        // gets cursor, gets first LatLang and then proceeds to loop the other data points
        public void getDataPoints() {
            Cursor cursor = this.dbHelper.getDataPoints(tripId);
            cursor.moveToFirst();

            // TODO: 07/10/2016 fix crash   Caused by: android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0
            prevLatLng = (new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_LATITUDE))),
                    (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_LONGITUDE))))));
            parseLatLng(cursor);

            drawLinesOnMap();
            /*initGoogleMap();*/
        }

        public void parseLatLng(Cursor cursor) {
            //Log.e(TAG, "getDataPoints: Moving to next");
            // get the current LatLang
            LatLng currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_LONGITUDE)))));
            //Log.e(TAG, "parseLatLng: cursor data " + Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_RPM))));
            if (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_RPM))) > 2500) {
                if (prevRPM > 2500) {
                    currentPolylineOption.add(currentLatLng);
                    prevLatLng = currentLatLng;
                } else {
                    polylineOptionsList.add(currentPolylineOption);
                    //Log.e(TAG, "parseLatLng: Added to polylines");
                    currentPolylineOption = new PolylineOptions();
                    currentPolylineOption.add(prevLatLng, currentLatLng);
                    currentPolylineOption = currentPolylineOption.color(Color.RED);
                    prevLatLng = currentLatLng;
                }
            } else {
                if (prevRPM > 2500) {
                    polylineOptionsList.add(currentPolylineOption);
                    //Log.e(TAG, "parseLatLng: Added to polylines");
                    // make new line
                    currentPolylineOption = new PolylineOptions();
                    currentPolylineOption.add(prevLatLng, currentLatLng);
                    currentPolylineOption = currentPolylineOption.color(Color.GREEN);
                    prevLatLng = currentLatLng;
                } else {
                    currentPolylineOption.add(currentLatLng);
                    prevLatLng = currentLatLng;
                }
            }

            while (cursor.moveToNext()) {
                //Log.e(TAG, "getDataPoints: Moving to next");
                currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_LONGITUDE)))));
                //Log.e(TAG, "parseLatLng: cursor data " + Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_RPM))));
                if (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_RPM))) > 2500) {
                    if (prevRPM > 2500) {
                        // use previous line
                        currentPolylineOption.add(currentLatLng);
                        prevLatLng = currentLatLng;
                    } else {
                        polylineOptionsList.add(currentPolylineOption);
                        //Log.e(TAG, "parseLatLng: Added to polylines");
                        // make new line red color
                        currentPolylineOption = new PolylineOptions();
                        currentPolylineOption.add(prevLatLng, currentLatLng);
                        currentPolylineOption = currentPolylineOption.color(Color.RED);
                        prevLatLng = currentLatLng;
                    }
                } else {
                    if (prevRPM > 2500) {
                        polylineOptionsList.add(currentPolylineOption);
                        //Log.e(TAG, "parseLatLng: Added to polylines");
                        // make new line
                        currentPolylineOption = new PolylineOptions();
                        currentPolylineOption.add(prevLatLng, currentLatLng);
                        currentPolylineOption = currentPolylineOption.color(Color.GREEN);
                        prevLatLng = currentLatLng;
                    } else {
                        currentPolylineOption.add(currentLatLng);
                        prevLatLng = currentLatLng;
                    }
                }
                prevRPM = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.DATAPOINT_RPM)));
                polylineOptionsList.add(currentPolylineOption);
            }
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
