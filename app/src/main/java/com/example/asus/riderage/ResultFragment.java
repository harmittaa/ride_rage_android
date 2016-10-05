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
    private TextView textView;
    private Constants.FRAGMENT_CALLER fragment_caller;
    private static long tripId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_result, container, false);
        textView = (TextView) fragmentView.findViewById(R.id.avgRpmResultLabel);
        textView.setText("It works");

        DataFetcher dataFetcher = new DataFetcher(tripId);
        dataFetcher.execute();

        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateFragmentView(final String update) {
        Log.e(TAG, "updateFragmentView: trying to update view");
        getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(update);
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
            String make = (cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_END_TIME))) + "\n" + cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.TRIP_AVERAGE_RPM));
            updateFragmentView(make);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }


    }
}
