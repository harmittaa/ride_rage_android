package com.example.asus.riderage.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by Daniel on 06/10/2016.
 */

public class TripsListFragment extends Fragment implements UpdatableFragment, AdapterView.OnItemClickListener {

    private View fragmentView;
    private ListView listView;
    TripDatabaseHelper dbHelper;
    private ArrayList<Long> tripIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_trips_list, container, false);
        this.listView = (ListView) fragmentView.findViewById(R.id.tripsListView);
        setupListView();
        //setupBackButtonActon();
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

        public TripsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
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
            TextView trip_cost = (TextView) view.findViewById(R.id.trip_cost);

            nameText.setText(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            durationText.setText(cursor.getString(cursor.getColumnIndexOrThrow("duration")));
            distanceText.setText(cursor.getString(cursor.getColumnIndexOrThrow("distance")));
            dateText.setText(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
            trip_cost.setText("N/A");
        }
    }

    @Override
    public void updateOnStateChanged(Constants.CONNECTION_STATE connection_state) {

    }

}
