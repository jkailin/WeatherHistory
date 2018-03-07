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

    public LocationTracker() {

    }

    // keeps track of location changes
    private class LocationListener implements android.location.LocationListener {
        Location lastLocation;


        public LocationListener(String provider) {
            lastLocation = new Location(provider);
        }

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

        // initialize location tracking and read/write
        locationListener = new LocationListener(LocationManager.GPS_PROVIDER);
        scheduler = Executors.newScheduledThreadPool(1);
        fileHelper = new FileHelper();

        // checks the GPS every 10 seconds
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0,
                    locationListener
            );
            //mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        } catch (java.lang.SecurityException e) {
            e.printStackTrace();

        }
        //mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListener,null);

        recordLocation(locationListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(locationListener);
            } catch (Exception e) {

            }
        }
    }


    public void recordLocation(final LocationListener locationListener) {
        final Runnable recorder = new Runnable() {
            public void run() {
                String weather = getWeather(locationListener.getLastLocation().getLatitude(), locationListener.getLastLocation().getLongitude());
                /*String s = locationListener.getLastLocation().getLatitude() + ","
                        + locationListener.getLastLocation().getLongitude() + "\n";*/
                fileHelper.writeFile(getApplicationContext(), weather);

            }
        };

        scheduler.scheduleAtFixedRate(recorder, 10, 10, SECONDS);
    }


    public String getWeather(double latitude, double longitude) {
        try {

            String url = "https://api.openweathermap.org/data/2.5/weather?APPID=" +
                    getApplicationContext().getString(R.string.weather_api_key) +
                    "&lat=" +
                    Double.toString(latitude) +
                    "&lon=" +
                    Double.toString(longitude);

            URL call = new URL(url);
            HttpsURLConnection myConnection = (HttpsURLConnection) call.openConnection();
            myConnection.setRequestMethod("GET");
            //myConnection.setConnectTimeout(5);

            // should work but doesn't
            /*myConnection.setRequestProperty("APPID", "944aaef369c68f1287a26953846af089");
            myConnection.setRequestProperty("lat", *//*Double.toString(latitude)*//*"33");
            myConnection.setRequestProperty("lon", *//*Double.toString(longitude)*//*"44");*/

            if (myConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStreamReader inputStreamReader = new InputStreamReader(myConnection.getInputStream(), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder responseString= new StringBuilder();
                String buf;

                while ((buf = bufferedReader.readLine()) != null)
                    responseString.append(buf);

                myConnection.disconnect();

                JSONObject response = new JSONObject(responseString.toString());
                JSONObject weather = response.getJSONArray("weather").getJSONObject(0);

                DateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
                String date = df.format(Calendar.getInstance().getTime());

                String dateAndWeather = date + "--(" + Double.toString(latitude) +
                        "," + Double.toString(longitude) + ")--"
                        + weather.getString("description");

                return dateAndWeather + "\n";
            } else {
                Log.e(TAG, "" + myConnection.getResponseCode());
                return "";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

}
