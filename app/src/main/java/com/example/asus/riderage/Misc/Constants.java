package com.example.asus.riderage.Misc;

/**
 * Created by Daniel on 04/10/2016.
 */

public class Constants {
    public enum CONNECTION_STATE{
        CONNECTED_NOT_RUNNING,DISCONNECTED,CONNECTED_RUNNING
    }
    public enum FRAGMENT_TYPES{
        GAUGES_FRAGMENT, RESULT_FRAGMENT, TRIPS_LIST_FRAGMENT
    }
    public enum FRAGMENT_CALLER{
        TRIP_END, TRIP_LIST
    }

}