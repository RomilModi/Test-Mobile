package com.example.rmodi.checkconstantlocation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.rmodi.checkconstantlocation.Adapter.AdapterAttandance;
import com.example.rmodi.checkconstantlocation.bean.Attandancebean;
import com.example.rmodi.checkconstantlocation.bean.UserAttendance;
import com.example.rmodi.checkconstantlocation.database.DatabaseHelper;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    String lat, lon;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private DatabaseHelper mDatabaseHelper;
    private RecyclerView recycler_view;
    private LinearLayoutManager layoutManager;
    private ArrayList<UserAttendance> mArraylstAttandance;
    private Attandancebean Attandancebean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mDatabaseHelper = new DatabaseHelper(getApplicationContext());

        mArraylstAttandance = new ArrayList<UserAttendance>();

        try {
            mDatabaseHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        recycler_view.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(layoutManager);


        if (Constants.isLocationEnabled(this)) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    callService();

                    getAttandance();
                }
            } else {
                callService();
                getAttandance();
            }
        } else {
            showSettingsAlert(this);
        }


//        buildGoogleApiClient();


    }

    public void getAttandance() {
        if (Constants.checkInternetConnection(this)) {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("user_id", "10");
                CheckInRequest(jObj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void callService() {
        Intent in = new Intent(this, BackgroundLocationService.class);
        startService(in);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;

        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Constants.showMessageDialogForMarshMallow(MainActivity.this, MainActivity.this.getResources().getString(R.string.app_name), "Please allow additional features in App settings for location permission to access location.");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callService();
                    getAttandance();
                }
                break;

        }
    }

    public void showSettingsAlert(final Context mContext) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Hipster");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void CheckInRequest(String request) {
        Log.e("request", "request : " + request);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, Constants.WS_URL_ATTANDANCE, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("Response", "Response : " + jsonObject.toString());

                        Gson gson = new Gson();
                        Attandancebean = gson.fromJson(jsonObject.toString(), Attandancebean.class);
                        mArraylstAttandance.addAll(Attandancebean.data.userAttendance);

                        AdapterAttandance mAdapterAttandance = new AdapterAttandance(MainActivity.this, mArraylstAttandance);
                        mAdapterAttandance.notifyDataSetChanged();
                        recycler_view.setAdapter(mAdapterAttandance);

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
