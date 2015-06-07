package com.ibm.bluelist;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ibm.mobile.services.cloudcode.IBMCloudCode;
import com.ibm.mobile.services.core.http.IBMHttpResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by prerit on 6/6/15.
 */
public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "Hello world";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","came here------------------------------------------");
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }


        displayLocation();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("asdf", "Tracking Service destroyed");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Service","in onConnected");
        displayLocation();
        final Handler handler = new Handler();
        final Runnable r = new Runnable()
        {
            public void run()
            {  displayLocation();
                // code here what ever is required
                handler.postDelayed(this, 12000);
            }
        };

        handler.postDelayed(r, 12000);

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }


    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

    }

   /*
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }


    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Toast.makeText(getApplicationContext(),
                        "Connection problems.", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_SHORT)
                        .show();

            }
            return false;
        }
        return true;
    }


    public void onResume() {


        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    public void onStop() {

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }







    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Toast.makeText(getApplicationContext(),
                    String.valueOf(latitude) + "," + String.valueOf(longitude), Toast.LENGTH_SHORT)
                    .show();
            Log.d("display", String.valueOf(latitude) + "," + String.valueOf(longitude));
            sendToCloud( String.valueOf(latitude),String.valueOf(longitude));


            //lblLocation.setText(latitude + ", " + longitude);

        } else {
            Toast.makeText(getApplicationContext(),
                    "Cannot find location", Toast.LENGTH_LONG)
                    .show();

            //lblLocation
                   // .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    private void sendToCloud(String latitude,String longitude) {

        // Initialize and retrieve an instance of the IBM CloudCode service.
        IBMCloudCode.initializeService();
        IBMCloudCode myCloudCodeService = IBMCloudCode.getService();
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("key1", "value1");
        } catch (JSONException e) {
            e.printStackTrace();
        }

		/*
		 * Call the node.js application hosted in the IBM Cloud Code service
		 * with a POST call, passing in a non-essential JSONObject.
		 * The URI is relative to/appended to the BlueMix context root.
		 */
        myCloudCodeService.get("/sendLocation?id=1234&lat="+latitude+"&long="+longitude).continueWith(new Continuation<IBMHttpResponse, Void>() {


            @Override
            public Void then(Task<IBMHttpResponse> task) throws Exception {
                if (task.isCancelled()) {
                    // Log.e(CLASS_NAME, "Exception : Task" + task.isCancelled() + "was cancelled.");
                } else if (task.isFaulted()) {
                    //  Log.e(CLASS_NAME, "Exception : " + task.getError().getMessage());
                } else {
                    InputStream is = task.getResult().getInputStream();
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(is));
                        String responseString = "";
                        String myString = "";
                        while ((myString = in.readLine()) != null)
                            responseString += myString;

                        in.close();
                        System.out.println( "Response Body: " + responseString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //  Log.i(CLASS_NAME, "Response Status from notifyOtherDevices: " + task.getResult().getHttpResponseCode());
                }

                return null;
            }

        });

          }

    }

