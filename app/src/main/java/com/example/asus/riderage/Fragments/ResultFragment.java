package com.example.asus.riderage.Fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.MainActivity;
import com.example.asus.riderage.R;
import com.example.asus.riderage.Misc.UpdatableFragment;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
*Handles the UI of the result screen, class includes AsyncTask for parsing data from SQLite
*/

public class ResultFragment extends Fragment implements UpdatableFragment, OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private final String TAG = "ResultFragment";
    private View fragmentView;
    private TextView durationTextView, distanceTextView, avgSpeedTextView, avgRpmTextView, textView;
    private Constants.FRAGMENT_CALLER fragment_caller;
    private static long tripId;
    private GoogleMap googleMap;
    private MapFragment mapFragment;
    ArrayList<PolylineOptions> polylineOptionsList = new ArrayList<>();
    ArrayList<LatLng> allLatLngs = new ArrayList<>();

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
        Log.e(TAG, "onMapReady: map is ready");
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);
        this.googleMap.setOnPolylineClickListener(this);
        DataFetcher dataFetcher = new DataFetcher(tripId);
        dataFetcher.execute();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate result fragment: 6.");
    }

    @Override
    public void onResume() {
        super.onResume();
        getMainActivity().changeActionBarIcons(Constants.FRAGMENT_TYPES.RESULT_FRAGMENT);
    }

    public void updateFragmentView(final String duration, final String distance, final String avgSpeed, final String avgRpm, final String placeHolder) {
        Log.e(TAG, "endTrip params:\ntripid " + tripId + "\ndistance " + distance + "\nduration " + duration + "\naveragespeed " + avgSpeed + "\naveragerpm " + avgRpm + "\nconsumption " + placeHolder);
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
    private void drawPolylines() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                googleMap.clear();
                Log.e(TAG, "run: freeze happens here");
                Log.e(TAG, "run: size" + polylineOptionsList.size());
                for (PolylineOptions po : polylineOptionsList) {
                    Log.e(TAG, "run: polyline data " + po.getPoints().size());
                    Log.e(TAG, "run: polyline data " + po.getPoints().get(1));
                    googleMap.addPolyline(po.clickable(true));
                }
                Log.e(TAG, "run: or here?");
                zoomMap();
            }
        });
    }

    private void zoomMap() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Log.e(TAG, "zoomMap: all lats size" + allLatLngs.size());
        for (LatLng l : allLatLngs) {
            builder.include(l);
        }
        LatLngBounds bounds = builder.build();

        int padding = 50; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.animateCamera(cu);
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

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.e(TAG, "onPolylineClick: clicked polyline " + polyline);
    }

    private class DataFetcher extends AsyncTask<Integer, Long, Boolean> {
        private long tripId;
        private TripDatabaseHelper dbHelper;
        /*private String duration;
        private String distance;*/
        double prevRPM = 0;
        LatLng prevLatLng;

        PolylineOptions currentPolylineOption = new PolylineOptions();

        // constructor to pass tripId
        public DataFetcher(long tripId) {
            this.tripId = tripId;
            polylineOptionsList.clear();
            allLatLngs.clear();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // gets trip data first, then gets datapoints
        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.e(TAG, "doInBackground: 7.");
            this.dbHelper = new TripDatabaseHelper(getContext());
            Cursor tripDataCursor = this.dbHelper.getFullTripData(getTripId());
            Cursor dataPointCursor = this.dbHelper.getDataPoints(getTripId());
            dataPointCursor.moveToFirst();
            tripDataCursor.moveToFirst();
            calculateAverages(dataPointCursor, tripDataCursor);
            getDataPoints(dataPointCursor);
            return null;
        }

        /**
         * Calculates averages
         *
         * @param dataPointCursor <p>Cursor holding DataPoints</p>
         * @param tripDataCursor  <p>Cursor holding the Trip data</p>
         */
        private void calculateAverages(Cursor dataPointCursor, Cursor tripDataCursor) {
            double avgSpeed = 0.0;
            double avgRpm = 0.0;
            int counter = 0;
            dataPointCursor.moveToFirst();
            tripDataCursor.moveToFirst();

            while (dataPointCursor.moveToNext()) {
                if (Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_SPEED))) > 0) {
                    counter++;
                    avgRpm += Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM)));
                    avgSpeed += Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_SPEED)));
                }
            }

            avgRpm = avgRpm / counter;
            avgSpeed = avgSpeed / counter;
            String duration = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_DURATION));
            String distance = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_DISTANCE));
            if (duration == null || distance == null) {
                duration = calculateDuration(dataPointCursor);
                distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", getDistanceFromCursor(dataPointCursor))) + "KM";
            }
            updateFragmentView(duration, distance, String.valueOf(String.format(Locale.getDefault(), "%.1f", avgSpeed)) + "KM/H", String.valueOf(String.format(Locale.getDefault(), "%.0f", avgRpm)) + "RPM", "jeeben");
        }

        /**
         * Calculates distance travelled based on coordinates from Cursor
         *
         * @param dataPointCursor <p>Cursor that holds DataPoints for the Trip</p>
         */
        private double getDistanceFromCursor(Cursor dataPointCursor) {
            double latitude;
            double longitude;
            double totalDistance = 0.0;
            Location location = new Location("ResultFragment");
            Location previousLocation = new Location("Make");
            dataPointCursor.moveToFirst();

            while (dataPointCursor.moveToNext()) {
                location.setLatitude(dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE)));
                location.setLongitude(dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)));
                if (previousLocation.getLatitude() != 0.0) {
                    totalDistance += location.distanceTo(previousLocation) / 1000;
                }

                previousLocation.setLatitude(location.getLatitude());
                previousLocation.setLongitude(location.getLongitude());
            }
            return totalDistance;
        }

        /**
         * Calculates the time between the first and the last DataPoints in Cursor.
         *
         * @param dataPointCursor <p>Cursor holding DataPoints</p>
         */
        private String calculateDuration(Cursor dataPointCursor) {
            String duration = "";
            dataPointCursor.moveToFirst();
            String startDate = dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_TIMESTAMP));
            dataPointCursor.moveToLast();
            String endDate = dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_TIMESTAMP));
            Log.e(TAG, "calculateDuration: enddate " + endDate + " start " + startDate);
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            try {
                Date startDateFormatted = dateFormat.parse(startDate);
                Date endDateFormatted = dateFormat.parse(endDate);
                duration = formatDuration(endDateFormatted.getTime() - startDateFormatted.getTime());
            } catch (ParseException e) {
                Log.e(TAG, "calculateAverages error ", e);
            }
            return duration;
        }

        /**
         * Formats milliseconds to HH:MM:SS
         *
         * @param tripTimeTotal <p>Parameter in MS to</p>
         */
        private String formatDuration(long tripTimeTotal) {
            String hms = String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(tripTimeTotal),
                    TimeUnit.MILLISECONDS.toMinutes(tripTimeTotal) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(tripTimeTotal) % TimeUnit.MINUTES.toSeconds(1));
            Log.e(TAG, "formatDuration: time formatted " + hms);
            return hms;
        }


        // gets cursor, gets first LatLang and then proceeds to loop the other data points
        private void getDataPoints(Cursor dataPointCursor) {
            dataPointCursor.moveToFirst();
            if (dataPointCursor.getCount() > 0) {
                prevLatLng = (new LatLng(Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE))),
                        (Double.parseDouble(dataPointCursor.getString(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE))))));
                parseLatLng(dataPointCursor);
                drawPolylines();
            } else {
                Log.e(TAG, "getDataPoints: no datapoints available");
            }
        }

        // Creates PolyLineOption objects from DataPoint rows retrieved from the cursor
        private void parseLatLng(Cursor cursor) {
            LatLng currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)))));
            while (currentLatLng.latitude == 0 && currentLatLng.longitude == 0) {
                cursor.moveToNext();
                currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)))));
                Log.e(TAG, "parseLatLng: latlong was at equator");
            }
            if (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM))) > 2500) {
                if (prevRPM > 2500) {
                    currentPolylineOption.add(currentLatLng);
                    currentPolylineOption = currentPolylineOption.color(Color.RED);
                    prevLatLng = currentLatLng;
                } else {
                    polylineOptionsList.add(currentPolylineOption);
                    currentPolylineOption = new PolylineOptions();
                    currentPolylineOption.add(prevLatLng, currentLatLng);
                    currentPolylineOption = currentPolylineOption.color(Color.RED);
                    prevLatLng = currentLatLng;
                }
            } else {
                if (prevRPM > 2500) {
                    polylineOptionsList.add(currentPolylineOption);
                    // make new line
                    currentPolylineOption = new PolylineOptions();
                    currentPolylineOption.add(prevLatLng, currentLatLng);
                    currentPolylineOption = currentPolylineOption.color(Color.GREEN);
                    prevLatLng = currentLatLng;
                } else {
                    currentPolylineOption.add(currentLatLng);
                    currentPolylineOption = currentPolylineOption.color(Color.GREEN);
                    prevLatLng = currentLatLng;
                }
            }

            while (cursor.moveToNext()) {
                currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)))));
                allLatLngs.add(currentLatLng);
                if (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM))) > 2500) {
                    if (prevRPM > 2500) {
                        // use previous line
                        currentPolylineOption.add(currentLatLng);
                        prevLatLng = currentLatLng;
                    } else {
                        polylineOptionsList.add(currentPolylineOption);
                        // make new line red color
                        currentPolylineOption = new PolylineOptions();
                        currentPolylineOption.add(prevLatLng, currentLatLng);
                        currentPolylineOption = currentPolylineOption.color(Color.RED);
                        prevLatLng = currentLatLng;
                    }
                } else {
                    if (prevRPM > 2500) {
                        polylineOptionsList.add(currentPolylineOption);
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
                prevRPM = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM)));
            }
            polylineOptionsList.add(currentPolylineOption);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }
    }
}
