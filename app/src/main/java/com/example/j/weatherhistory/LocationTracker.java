package com.example.j.weatherhistory;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.HttpsURLConnection;

import static android.content.ContentValues.TAG;
import static java.util.concurrent.TimeUnit.SECONDS;

public class LocationTracker extends Service {

    private ScheduledExecutorService scheduler;

    private LocationManager mLocationManager = null;
    FileHelper fileHelper;
    LocationListener locationListener;

    // keeps track of location changes using device GPS
    private class LocationListener implements android.location.LocationListener {
        Location lastLocation;


        public LocationListener(String provider) {
            lastLocation = new Location(provider);
        }

        // update location if a location change is detected
        @Override
        public void onLocationChanged(Location location) {
            lastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public Location getLastLocation() {
            return lastLocation;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        // initialize scheduled location tracking and file read/write
        locationListener = new LocationListener(LocationManager.GPS_PROVIDER);
        scheduler = Executors.newScheduledThreadPool(1);
        fileHelper = new FileHelper();

        // checks the GPS at certain interval. Set to 10s for testing.
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0,
                    locationListener
            );

        } catch (java.lang.SecurityException e) {
            e.printStackTrace();

        }

        recordLocation(locationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // ends long running tasks
        scheduler.shutdown();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(locationListener);
            } catch (Exception e) {

            }
        }
    }

    // starts scheduler to record locations at an intervals
    public void recordLocation(final LocationListener locationListener) {
        final Runnable recorder = new Runnable() {
            public void run() {
                String weather = getWeather(locationListener.getLastLocation().getLatitude(),
                        locationListener.getLastLocation().getLongitude());
                fileHelper.writeFile(getApplicationContext(), weather);

            }
        };

        scheduler.scheduleAtFixedRate(recorder, 3, 10, SECONDS);
    }

    // returns weather information at a particular latitude/longitude
    public String getWeather(double latitude, double longitude) {
        try {
            // url for REST API call
            String url = "https://api.openweathermap.org/data/2.5/weather?APPID=" +
                    BuildConfig.WEATHER_KEY +
                    "&lat=" +
                    Double.toString(latitude) +
                    "&lon=" +
                    Double.toString(longitude);

            // setup connection parameters with secure connection
            URL call = new URL(url);
            HttpsURLConnection connection = (HttpsURLConnection) call.openConnection();
            connection.setRequestMethod("GET");
            //myConnection.setConnectTimeout(5);

            // check that the call has a valid response
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // read the information from the call
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder responseString= new StringBuilder();
                String buf;

                while ((buf = bufferedReader.readLine()) != null)
                    responseString.append(buf);

                connection.disconnect();

                // convert to JSON to be parsed
                JSONObject response = new JSONObject(responseString.toString());
                JSONObject weather = response.getJSONArray("weather").getJSONObject(0);

                // get timestamp for the location call
                DateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());

                // combine time and weather information
                String dateAndWeather = date + "--(" + Double.toString(latitude) +
                        "," + Double.toString(longitude) + ")--"
                        + weather.getString("description");

                return dateAndWeather + "\n";
            } else {
                Log.e(TAG, "" + connection.getResponseCode());
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

}
