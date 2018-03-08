package com.example.j.weatherhistory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private TextView dataBody;
    private TextView refreshStamp;
    private FileHelper fileHelper;
    private DateFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Textboxes
        dataBody = (TextView) findViewById(R.id.message);
        refreshStamp = (TextView) findViewById(R.id.refreshStamp);

        df = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
        fileHelper = new FileHelper();

        // Check if application has GPS permission.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            displayData(fileHelper.readFile(this));
            startService(new Intent(this, LocationTracker.class));
            Log.e(TAG, "Service Started");

        } else {
            // ask for GPS permission, only permission needed
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
                sendEmail(fileHelper.readFile(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Show weather history
    public void displayData(String s) {
        dataBody.setText(s);
        String data = "Last Refreshed: " + df.format(Calendar.getInstance().getTime());
        refreshStamp.setText(data);
    }

    // Show message asking for GPS
    private void requestPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

    }

    // Start GPS tracking service if GPS permission was granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayData(fileHelper.readFile(this));
                startService(new Intent(this, LocationTracker.class));
            } else {
                Log.e(TAG, "no GPS, SMS permission");

            }
        }
    }

    // Sends data as an email
    public void sendEmail(String s){
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, s);
            // user is asked to choose a proper email application
            this.startActivity(Intent.createChooser(emailIntent,"Sending..."));

        } catch(Exception e) {
            Toast toast = Toast.makeText(this, "couldn't send", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

}

