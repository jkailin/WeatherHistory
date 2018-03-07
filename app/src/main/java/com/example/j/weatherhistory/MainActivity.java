package com.example.j.weatherhistory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private TextView mMsgView;
    private FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileHelper = new FileHelper();


        mMsgView = (TextView) findViewById(R.id.message);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            displayData(fileHelper.readFile(this));
            startService(new Intent(this, LocationTracker.class));
            Log.e(TAG, "Service Started");

        } else {
            requestPermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.refresh:
                displayData(fileHelper.readFile(this));
                return true;
            case R.id.delete:
                fileHelper.deleteFile(this);
                displayData(fileHelper.readFile(this));
                return true;
            case R.id.email:
                sendSMS(fileHelper.readFile(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void displayData(String s) {

        mMsgView.setText(s);
    }

    // Show message asking for GPS
    private void requestPermission() {

        Snackbar.make(findViewById(R.id.container), "no GPS", Snackbar.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, LocationTracker.class));
            } else {
                Log.e(TAG, "no gps permission");

            }
        }
    }

    public void sendSMS(String s){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

    }

}

