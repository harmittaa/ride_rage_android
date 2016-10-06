package com.example.asus.riderage;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.asus.riderage.Database.TripDatabaseHelper;


public class ResultFragment extends Fragment implements UpdatableFragment {
    private final String TAG = "ResultFragment";
    private View fragmentView;
    private TextView durationTextView,distanceTextView,avgSpeedTextView,avgRpmTextView,textView;
    private Constants.FRAGMENT_CALLER fragment_caller;
    private static long tripId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_result, container, false);
        textView = (TextView) fragmentView.findViewById(R.id.avgRpmResultLabel);
        textView.setText("It works");


        durationTextView = (TextView)fragmentView.findViewById(R.id.durationResultLabel);
        distanceTextView = (TextView)fragmentView.findViewById(R.id.distanceResultLabel);
        avgSpeedTextView = (TextView)fragmentView.findViewById(R.id.avgSpeedResultLabel);
        avgRpmTextView = (TextView)fragmentView.findViewById(R.id.avgRpmResultLabel);

        DataFetcher dataFetcher = new DataFetcher(tripId);
        dataFetcher.execute();

        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateFragmentView(final String duration, final String distance, final String avgSpeed, final String avgRpm, final String placeHolder) {
        Log.e(TAG, "updateFragmentView: trying to update view");
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                durationTextView.setText(duration);
                distanceTextView.setText(distance);
                avgSpeedTextView.setText(avgSpeed);
                avgRpmTextView.setText(avgRpm);
                textView.setText(placeHolder);
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

    private class DataFetcher extends AsyncTask<Integer, Long, Boolean> {
        private long tripId;

        public DataFetcher(long tripId) {
            this.tripId = tripId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show pop up here
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            TripDatabaseHelper dbHelper = new TripDatabaseHelper(getContext());
            Cursor cursor = dbHelper.getFullTripData(getTripId());
            cursor.moveToFirst();
            Log.e(TAG, "doInBackground: num of stufs:" + cursor.getCount() + "\n Trip Id: " + this.tripId);
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_DURATION_MS)) + "MS";
            String distance = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_DISTANCE)) + "KM";
            String avgSpd = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_SPEED)) + "KM/H";
            String avgrpm = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_RPM)) + "RPM";
            updateFragmentView(avgrpm
                    ,distance,avgSpd,avgrpm,"jeeben");
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }


    }
}
