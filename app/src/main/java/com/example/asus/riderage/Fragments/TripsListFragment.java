package com.example.asus.riderage.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.asus.riderage.Database.TripDatabaseHelper;
import com.example.asus.riderage.MainActivity;
import com.example.asus.riderage.Misc.Constants;
import com.example.asus.riderage.Misc.UpdatableFragment;
import com.example.asus.riderage.R;
import com.example.asus.riderage.Services_and_Handlers.CommunicationHandler;

import java.util.ArrayList;

/**
 * Created by Daniel on 06/10/2016.
 * <p>Displays a list of previous trips taken on the current device</p>
 */

public class TripsListFragment extends Fragment implements UpdatableFragment, AdapterView.OnItemClickListener {

    private ListView listView;
    TripDatabaseHelper dbHelper;
    private ArrayList<Long> tripIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_trips_list, container, false);
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
        this.listView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMainActivity().changeActionBarIcons(Constants.FRAGMENT_TYPES.TRIPS_LIST_FRAGMENT);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CommunicationHandler.getCommunicationHandlerInstance().setTripId(tripIds.get(position));
        getMainActivity().changeVisibleFragmentType(Constants.FRAGMENT_TYPES.RESULT_FRAGMENT,true);
    }



    private MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }


    private class TripsCursorAdapter extends CursorAdapter {

        TripsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.result_list_table_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            tripIds.add(Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow("_id"))));

            TextView nameText = (TextView) view.findViewById(R.id.trip_name);
            TextView durationText = (TextView) view.findViewById(R.id.trip_duration);
            TextView distanceText = (TextView) view.findViewById(R.id.trip_distance);
            TextView dateText = (TextView) view.findViewById(R.id.trip_date);

            nameText.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            String durationString = "Duration: " + cursor.getString(cursor.getColumnIndexOrThrow("duration"));
            durationText.setText(durationString);
            distanceText.setText("Distance driven: " + cursor.getString(cursor.getColumnIndexOrThrow("distance")) + " km");
            dateText.setText(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
        }
    }

    @Override
    public void updateOnStateChanged(Constants.CONNECTION_STATE connection_state) {

    }

}
