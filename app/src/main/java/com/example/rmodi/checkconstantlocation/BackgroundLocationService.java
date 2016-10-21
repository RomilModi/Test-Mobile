package com.example.rmodi.checkconstantlocation;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.rmodi.checkconstantlocation.bean.PunchInOutbean;
import com.example.rmodi.checkconstantlocation.database.DatabaseHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by rmodi on 10/19/2016.
 */

public class BackgroundLocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    IBinder mBinder = new LocalBinder();

    private GoogleApiClient mGoogleApiClient;
    private PowerManager.WakeLock mWakeLock;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private Location mLocation;

    private Boolean servicesAvailable = false;

    private DatabaseHelper mDatabaseHelper;


    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mDatabaseHelper = new DatabaseHelper(this);

        mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

        servicesAvailable = servicesConnected();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        setUpLocationClientIfNeeded();


    }

    /*
     * Create a new location client, using the enclosing class to
     * handle callbacks.
     */
    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
        } else {

            return false;
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);

        /*
        WakeLock is reference counted so we don't want to create multiple WakeLocks. So do a check before initializing and acquiring.
        This will fix the "java.lang.Exception: WakeLock finalized while still held: MyWakeLock" error that you may find.
        */
        if (this.mWakeLock == null) { //**Added this
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }

        if (!this.mWakeLock.isHeld()) { //**Added this
            this.mWakeLock.acquire();
        }

        if (!servicesAvailable || mGoogleApiClient.isConnected() || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !mInProgress) {
            mInProgress = true;
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }


    private void setUpLocationClientIfNeeded() {
        Log.e("mGoogleApiClient ", "mGoogleApiClient  : " + mGoogleApiClient);
        if (mGoogleApiClient == null)
            buildGoogleApiClient();
    }

    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("debug", msg);

        if (Constants.checkInternetConnection(this)) {
            drawMarkerWithCircle(new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            float[] results = new float[1];
            Location.distanceBetween(Float.valueOf("23.044680"), Float.valueOf("72.540965"), location.getLatitude(), location.getLongitude(), results);
            float distanceInMeters = results[0];
            boolean isWithin500mtr = distanceInMeters < 10;

            PunchInOutbean p = new PunchInOutbean();

            p.setUSER_ID("10");

            if (isWithin500mtr) {
                p.setUSER_ACTIVITY("1");
            } else {
                p.setUSER_ACTIVITY("0");
            }

            p.setDATE(getTime());
            p.setLATITUDE(String.valueOf(location.getLatitude()));
            p.setLONGITUDE(String.valueOf(location.getLongitude()));

            mDatabaseHelper.openDataBase();
            mDatabaseHelper.insertDetails(p.getUSER_ID(), p.getUSER_ACTIVITY(), p.getDATE(), p.getLATITUDE(), p.getLONGITUDE());
            mDatabaseHelper.close();


        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public String getTime() {
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return mDateFormat.format(new Date());
    }

    public void appendLog(String text, String filename) {
        File logFile = new File(filename);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Turn off the request flag
        this.mInProgress = false;

        if (this.servicesAvailable && this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            // Destroy the current location client
            this.mGoogleApiClient = null;
        }
        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ":
        // Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();

        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }

        super.onDestroy();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {

        // Request location updates using static settings
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        mLocationRequest.setFastestInterval(10000000);
//        mLocationRequest.setInterval(10000000); // Update location every second

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.e("TAG", "onConnected : " + mLocation + " & Lat : " + mLocation.getLatitude());

//        Toast.makeText(this, "Lat is : " + mLocation.getLatitude() + " & Long : " + mLocation.getLongitude(), Toast.LENGTH_LONG).show();


        if (Constants.checkInternetConnection(this)) {
            drawMarkerWithCircle(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        } else {
            float[] results = new float[1];
            Location.distanceBetween(Float.valueOf("23.044680"), Float.valueOf("72.540965"), mLocation.getLatitude(), mLocation.getLongitude(), results);
            float distanceInMeters = results[0];
            boolean isWithin500mtr = distanceInMeters < 10;

            PunchInOutbean p = new PunchInOutbean();

            p.setUSER_ID("10");

            if (isWithin500mtr) {
                p.setUSER_ACTIVITY("1");
            } else {
                p.setUSER_ACTIVITY("0");
            }

            p.setDATE(getTime());
            p.setLATITUDE(String.valueOf(mLocation.getLatitude()));
            p.setLONGITUDE(String.valueOf(mLocation.getLongitude()));

            mDatabaseHelper.openDataBase();
            mDatabaseHelper.insertDetails(p.getUSER_ID(), p.getUSER_ACTIVITY(), p.getDATE(), p.getLATITUDE(), p.getLONGITUDE());
            mDatabaseHelper.close();


        }

//        appendLog(DateFormat.getDateTimeInstance().format(new Date()) + ": Connected", Constants.LOG_FILE);

    }

    /*
 * Called by Location Services if the connection to the
 * location client drops because of an error.
 */
    @Override
    public void onConnectionSuspended(int i) {
        mInProgress = false;
        mGoogleApiClient = null;
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            // If no resolution is available, display an error dialog
        } else {

        }
    }

    private void drawMarkerWithCircle(LatLng position) {

        float[] results = new float[1];
        Location.distanceBetween(Float.valueOf("23.044680"), Float.valueOf("72.540965"), position.latitude, position.longitude, results);
        float distanceInMeters = results[0];
        boolean isWithin500mtr = distanceInMeters < 10;
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("user_id", "10");
            if (isWithin500mtr) {
                jObj.put("user_activity", "1");
            } else {
                jObj.put("user_activity", "0");
            }
            jObj.put("date", getTime());
            CheckInRequest(jObj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Toast.makeText(this, "is Within 5 KM " + isWithin500mtr, Toast.LENGTH_SHORT).show();

    }

    private void CheckInRequest(String request) {
        Log.e("request", "request : " + request);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, Constants.WS_URL, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("Response", "Response : " + jsonObject.toString());

//                        if (!mArraylstLogin.isEmpty()) {
//                            mArraylstLogin.clear();
//                        }
//                        Gson gson = new Gson();
//                        Loginbean = gson.fromJson(jsonObject.toString(), Loginbean.class);

                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) /*{

            //            Passing some request headers
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
//                headers.put("Token", "10.10.10.113_Guest_7690e408-66d5-4f76-88cf-9c5002e8f707");
                return headers;
            }

        }*/;
        req.setRetryPolicy(new DefaultRetryPolicy(100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyApplication.getInstance().addToRequestQueue(req);


    }

}
