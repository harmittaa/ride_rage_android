package com.example.asus.riderage.Fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineRadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
 * Handles the UI of the result screen, class includes AsyncTask for parsing data from SQLite
 */

public class ResultFragment extends Fragment implements UpdatableFragment, OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private final String TAG = "ResultFragment";
    private View fragmentView;
    private TextView durationTextView, distanceTextView, avgSpeedTextView, avgRpmTextView, topSpeedTextView, topRpmTextView;
    private Constants.FRAGMENT_CALLER fragment_caller;
    private static long tripId;
    private GoogleMap googleMap;
    private MapFragment mapFragment;
    ArrayList<PolylineOptions> polylineOptionsList = new ArrayList<>();
    ArrayList<LatLng> allLatLngs = new ArrayList<>();
    LineChart lineChart;
    ArrayList<ILineDataSet> chartData = new ArrayList<>();
    ArrayList<Entry> chartDataRpm = new ArrayList<>();
    ArrayList<Entry> chartDataSpeed = new ArrayList<>();
    private String subtitle = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setTripId(CommunicationHandler.getCommunicationHandlerInstance().getTripId());
        fragmentView = inflater.inflate(R.layout.fragment_result, container, false);

        mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        durationTextView = (TextView) fragmentView.findViewById(R.id.durationResultLabel);
        distanceTextView = (TextView) fragmentView.findViewById(R.id.distanceResultLabel);
        avgSpeedTextView = (TextView) fragmentView.findViewById(R.id.avgSpeedResultLabel);
        avgRpmTextView = (TextView) fragmentView.findViewById(R.id.avgRpmResultLabel);
        topRpmTextView = (TextView) fragmentView.findViewById(R.id.topRpmResultLabel);
        topSpeedTextView = (TextView) fragmentView.findViewById(R.id.topSpeedResultLabel);

        lineChart = (LineChart) fragmentView.findViewById(R.id.chart1);

        return fragmentView;
    }

    /**
     * Called when GoogleMaps is ready to be initialized
     *
     * @param googleMap
     */
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
    public void onPause() {
        CommunicationHandler.getCommunicationHandlerInstance().getContext().resetActionBarTitle();
        super.onPause();
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
        if(subtitle != null) CommunicationHandler.getCommunicationHandlerInstance().getContext().getSupportActionBar().setSubtitle(subtitle);
        lineChart.invalidate();
    }

    /**
     * Handles updating values into the TextViews of the fragment
     *
     * @param duration total duration of the trip
     * @param distance total distance driven during the test
     * @param avgSpeed average speed of the trip
     * @param avgRpm   average rpm of the trip
     */
    public void updateFragmentView(final String duration, final String distance, final String avgSpeed, final String avgRpm) {
        Log.e(TAG, "endTrip params:\ntripid " + tripId + "\ndistance " + distance + "\nduration " + duration + "\naveragespeed " + avgSpeed + "\naveragerpm " + avgRpm + "\nconsumption ");
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ResultFragment.this.durationTextView.setText(duration);
                ResultFragment.this.distanceTextView.setText(distance);
                ResultFragment.this.avgSpeedTextView.setText(avgSpeed);
                ResultFragment.this.avgRpmTextView.setText(avgRpm);
                ResultFragment.this.setupChart();
            }
        });
    }

    /**
     * Separate method from <code>updateFragmentView</code> just for top values of trip, since they are retrieved from elsewhere
     * @param topSpeed Highest speed of the trip
     * @param topRpm Highest RPM of the trip
     */

    public void updateTopValues(final Double topSpeed, final Double topRpm){
        ResultFragment.this.topSpeedTextView.setText(topSpeed + "KM/H");
        ResultFragment.this.topRpmTextView.setText(topRpm + "RPM");
    }

    /**
     * Calls <code>runOnUiThread()</code> to draw <code>PolyLineOptions</code> on the Google Maps.
     * Afterwards calls {@link #zoomMap()}
     */
    private void drawPolylines() {
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                googleMap.clear();
                Log.e(TAG, "run: polylines size : " + polylineOptionsList.size());
                for (PolylineOptions po : polylineOptionsList) {
                    googleMap.addPolyline(po.clickable(true));
                }
                zoomMap();
            }
        });
    }

    /**
     * Uses <code>LatLngBounds</code> to calculate the bounds of the trip and creates <code>CameraUpdate</code> to zoom on the
     * location.
     */
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

    private void setupChart() {
        fetchDataForChart();
        LineData data = new LineData(chartData);
        lineChart.setData(data);
        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisRight().setEnabled(true);
        lineChart.setDescription("");
        lineChart.setDrawGridBackground(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxis(YAxis.AxisDependency.LEFT).setDrawGridLines(false);
        lineChart.getAxis(YAxis.AxisDependency.RIGHT).setDrawGridLines(true);
        lineChart.setMaxVisibleValueCount(4);
        Legend l = lineChart.getLegend();
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.LINE);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setYOffset(20);
        //l.setYOffset(-20);
        lineChart.invalidate();
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

    private void fetchDataForChart() {
        TripDatabaseHelper dbh = new TripDatabaseHelper(getMainActivity());
        Cursor dataPointCursor = dbh.getDataPoints(CommunicationHandler.getCommunicationHandlerInstance().getTripId());
        int counter = 0;
        Double currentSpeed, currentRpm, topRpm, topSpeed;
        currentRpm = currentSpeed = 0.0;
        topSpeed = topRpm = 0.0;
        while (dataPointCursor.moveToNext()) {
            currentRpm = dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM));
            currentSpeed = dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_SPEED));
            if (currentRpm > topRpm) topRpm = currentRpm;
            if (currentSpeed > topSpeed) topSpeed = currentSpeed;
            chartDataRpm.add(new Entry((counter * 2), dataPointCursor.getFloat(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM))));
            chartDataSpeed.add(new Entry((counter * 2), dataPointCursor.getFloat(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_SPEED))));
            counter++;
        }

        updateTopValues(topSpeed,topRpm);

        LineDataSet jeeben = new LineDataSet(chartDataRpm, "RPM");
        jeeben.setDrawCircles(false);
        jeeben.setColor(Color.RED);
        jeeben.setAxisDependency(YAxis.AxisDependency.LEFT);
        jeeben.setDrawValues(true);
        jeeben.setLineWidth(1.5f);
        LineDataSet huuben = new LineDataSet(chartDataSpeed, "Speed");
        huuben.setColor(Color.BLUE);
        huuben.setDrawCircles(false);
        huuben.setAxisDependency(YAxis.AxisDependency.RIGHT);
        huuben.setDrawValues(true);
        huuben.setLineWidth(1.5f);
        chartData.add(jeeben);
        chartData.add(huuben);
    }

    /**
     * Async Task for initialization of result view
     * draws <code>PolyLineOptions</code> and populates the <code>TextViews</code>
     */

    private class DataFetcher extends AsyncTask<Integer, Long, Boolean> {
        private long tripId;
        private TripDatabaseHelper dbHelper;
        double prevRPM = 0;
        LatLng prevLatLng;
        PolylineOptions currentPolylineOption = new PolylineOptions();

        /**
         * Constructor to pass the trip id
         *
         * @param tripId
         */
        public DataFetcher(long tripId) {
            this.tripId = tripId;
            polylineOptionsList.clear();
            allLatLngs.clear();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * doInBackground start that calls DbHelper for cursors and proceeds to call needed methods
         *
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.e(TAG, "doInBackground: 7.");
            this.dbHelper = new TripDatabaseHelper(getContext());
            final Cursor tripDataCursor = this.dbHelper.getFullTripData(getTripId());
            Cursor dataPointCursor = this.dbHelper.getDataPoints(getTripId());
            dataPointCursor.moveToFirst();
            tripDataCursor.moveToFirst();
            CommunicationHandler.getCommunicationHandlerInstance().getContext().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tripDataCursor.getCount() > 0) {
                        subtitle = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(dbHelper.TRIP_TITLE));
                        CommunicationHandler.getCommunicationHandlerInstance().getContext().getSupportActionBar().setSubtitle(subtitle);
                    }
                }
            });
            calculateAverages(dataPointCursor, tripDataCursor);
            getDataPoints(dataPointCursor);
            return null;
        }

        /**
         * Checks if the average speed and RPM are directly available
         * if not then calculates them based on DataPoint cursor.
         * <p>Calls {@link #calculateDuration(Cursor)} and {@link #getDistanceFromCursor(Cursor)} if duration / distance is not directly available.</p>
         *
         * @param dataPointCursor Cursor holding DataPoints
         * @param tripDataCursor  Cursor holding the Trip data
         */
        private void calculateAverages(Cursor dataPointCursor, Cursor tripDataCursor) {
            double avgSpeed = 0.0;
            double avgRpm = 0.0;
            int counter = 0;
            dataPointCursor.moveToFirst();
            tripDataCursor.moveToFirst();
            String avgrpm = "";
            String avgspd = "";
            Double toppestSpeed, topRpm, currentSpeed, currentRpm;
            toppestSpeed = topRpm = currentRpm = currentSpeed = 0.0;
            try {
                avgrpm = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_AVERAGE_RPM));
                avgspd = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_AVERAGE_SPEED));
                if (TextUtils.isEmpty(avgrpm) || TextUtils.isEmpty(avgspd)) {
                    while (dataPointCursor.moveToNext()) {
                        counter++;
                        currentRpm = dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_RPM));
                        currentSpeed = dataPointCursor.getDouble(dataPointCursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_SPEED));
                        avgSpeed += currentSpeed;
                        avgRpm += currentRpm;
                        Log.e(TAG, "calculateAverages: parsed from database rpm" + avgRpm);
                    }
                    if (avgRpm != 0) avgRpm = avgRpm / counter;
                    if (avgSpeed != 0) avgSpeed = avgSpeed / counter;

                } else {
                    avgRpm = Double.parseDouble(avgrpm);
                    avgSpeed = Double.parseDouble(avgspd);
                }
            } catch (CursorIndexOutOfBoundsException e) {
                Log.e(TAG, "calculateAverages: CURSOR EMPTY");
            }
            String duration = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_DURATION));
            String distance = tripDataCursor.getString(tripDataCursor.getColumnIndexOrThrow(TripDatabaseHelper.TRIP_DISTANCE));
            if (duration == null || distance == null) {
                duration = calculateDuration(dataPointCursor);
                distance = String.valueOf(String.format(Locale.getDefault(), "%.2f", getDistanceFromCursor(dataPointCursor))) + "KM";
            }
            Log.e(TAG, "endTrip params:\ntripid " + tripId + "\ndistance " + distance + "\nduration " + duration + "\naveragespeed " + avgspd + "\naveragerpm " + avgrpm + "\nconsumption ");
            updateFragmentView(duration, distance + "KM", String.valueOf(String.format(Locale.getDefault(), "%.1f", avgSpeed)) + "KM/H", String.valueOf(String.format(Locale.getDefault(), "%.0f", avgRpm)) + "RPM");
            //updateFragmentView(duration, distance + "KM", avgspd + "KM/H", avgrpm + "RPM", "jeeben");
        }

        /**
         * Calculates distance travelled based on coordinates from Cursor
         *
         * @param dataPointCursor <p>Cursor that holds DataPoints for the Trip</p>
         */
        private double getDistanceFromCursor(Cursor dataPointCursor) {
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
            DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
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
         * @param tripTimeTotal Milliseconds to the formatted
         */
        private String formatDuration(long tripTimeTotal) {
            String hms = String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(tripTimeTotal),
                    TimeUnit.MILLISECONDS.toMinutes(tripTimeTotal) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(tripTimeTotal) % TimeUnit.MINUTES.toSeconds(1));
            Log.e(TAG, "formatDuration: time formatted " + hms);
            return hms;
        }


        /**
         * Sets the first LatLng object then calls for LatLng parsing
         *
         * @param dataPointCursor Cursor that holds the trips Datapoints
         */
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

        /**
         * Creates PolyLineOption objects with a certain color, adds LatLngs to those objects and
         * saves them into polyLineOpetionsList
         *
         * @param cursor Cursor that holds the DataPoint objects
         */
        private void parseLatLng(Cursor cursor) {
            LatLng currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)))));
            while (currentLatLng.latitude == 0 && currentLatLng.longitude == 0) {
                cursor.moveToNext();
                currentLatLng = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LATITUDE))), (Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(TripDatabaseHelper.DATAPOINT_LONGITUDE)))));
                allLatLngs.add(currentLatLng);
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
