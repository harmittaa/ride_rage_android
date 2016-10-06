package com.example.asus.riderage.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Misc.UpdatableFragment;
import com.example.asus.riderage.R;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;

/**
 * Created by Daniel on 06/10/2016.
 */

public class TripsListView extends Fragment implements UpdatableFragment {

    private View fragmentView;
    private ListView listView;
    TripDatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_trips_list, container, false);
        this.listView = (ListView) fragmentView.findViewById(R.id.tripsListView);
        setupListView();
        return fragmentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new TripDatabaseHelper(CommunicationHandler.getCommunicationHandlerInstance().getContext());
    }

    private void setupListView() {
        TripsCursorAdapter jeebenAdapter = new TripsCursorAdapter(CommunicationHandler.getCommunicationHandlerInstance().getContext(),dbHelper.getTripHeaders(),false);
        this.listView.setAdapter(jeebenAdapter);
    }

    private class TripsCursorAdapter extends CursorAdapter {

        public TripsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.result_list_table_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView nameText = (TextView) view.findViewById(R.id.trip_name);
            TextView durationText = (TextView) view.findViewById(R.id.trip_duration);
            TextView distanceText = (TextView) view.findViewById(R.id.trip_distance);
            TextView dateText = (TextView) view.findViewById(R.id.trip_date);
            TextView trip_cost = (TextView) view.findViewById(R.id.trip_cost);

            nameText.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            durationText.setText(cursor.getString(cursor.getColumnIndexOrThrow("duration")));
            distanceText.setText(cursor.getString(cursor.getColumnIndexOrThrow("distance")));
            dateText.setText(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
            trip_cost.setText("90€");
        }
    }

    @Override
    public void updateOnStateChanged(Constants.CONNECTION_STATE connection_state) {

    }

}
