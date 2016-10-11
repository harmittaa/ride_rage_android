package com.example.asus.riderage.Database;

import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by Asus on 11/10/2016.
 */

public class DatabaseExport {
    private static final String TAG = "DatabaseExport";
    private static final String SAMPLE_DB_NAME = "TripDatabase";

    public DatabaseExport() {
        Log.e(TAG, "DatabaseExport: created"  );
    }

    public void exportDB(){
        Log.e(TAG, "exportDB: started" );
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String currentDBPath = "/data/com.example.asus.riderage/databases/" + SAMPLE_DB_NAME ;
        String backupDBPath = SAMPLE_DB_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Log.e(TAG, "exportDB: done");
        } catch(IOException e) {
            Log.e(TAG, "exportDB: something failed", e);
        }
    }


}
