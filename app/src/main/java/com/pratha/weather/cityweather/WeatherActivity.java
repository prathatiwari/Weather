package com.pratha.weather.cityweather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.pratha.weather.R;
import com.pratha.weather.network.WeatherDataServer;
import com.pratha.weather.util.LocationContract;

/**
 * Weather activity.
 * initializes view and presenter.
 * provides location data
 * <p/>
 * Created by Pratha on 8/22/2016.
 */
public class WeatherActivity extends AppCompatActivity
        implements LocationContract.WeatherLocationProvider,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = "WeatherActivity";
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_LOCATION = 10;
    private static final int REQUEST_CHECK_SETTINGS = 4;
    private LocationContract.LocationProviderCallback mLocationProviderCallback;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        WeatherFragment weatherFragment = (WeatherFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (weatherFragment == null) {
            // Create the fragment
            weatherFragment = new WeatherFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.contentFrame, weatherFragment);
            transaction.commit();
        }
        new WeatherPresenter(new WeatherDataServer(this), weatherFragment, this);
    }

    /**
     * method from WeatherLocationProvider this will return results to locationProviderCallback
     *
     * @param locationProviderCallback callback to provide result
     */
    @Override
    public void getCurrentLocation(LocationContract.LocationProviderCallback locationProviderCallback) {
        mLocationProviderCallback = locationProviderCallback;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this, this, this)
                    .addApi(LocationServices.API).build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //check for location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //we do not have permission

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                mGoogleApiClient.disconnect();
                mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_LOCATION_PERMISSION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            }
            return;
        }

        //we have permission, try n get the location
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        verifyLocation(lastLocation);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_PLAYSERVICE_CONNECTION_FAILED);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_PLAYSERVICE_CONNECTION_SUSPENDED);
    }

    @SuppressWarnings("MissingPermission")// we have already checked for permission
    private void verifyLocation(Location location) {
        if (location != null) {//got location go ahead
            mGoogleApiClient.disconnect();
            mLocationProviderCallback.onGetLocation(location.getLatitude() + "", location.getLongitude() + "");
        } else {// check and try to enable location in settings
            checkLocationSettings();
        }
    }

    /*
     * check and try to enable gps
     */
    private void checkLocationSettings() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(5000);// 5 seconds
        mLocationRequest.setNumUpdates(1);// we need only 1 update
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        locationSettingsRequest
                );
        result.setResultCallback(this);
    }


    @SuppressWarnings("MissingPermission")// suppress false positive warning
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Got permission, try nad get location
                Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                mLocationProviderCallback.onGetLocation(lastLocation.getLatitude() + "", lastLocation.getLongitude() + "");
            } else {//permission denied
                mGoogleApiClient.disconnect();
                mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_LOCATION_PERMISSION);
            }
        }
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:// settings are correct
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:// settings need to be changed
                try {
                    status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_ENABLE_LOCATION);
                    mGoogleApiClient.disconnect();
                    Log.w(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:// settings can not be changed
                mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_ENABLE_LOCATION);
                mGoogleApiClient.disconnect();
                break;
        }
    }

    @SuppressWarnings("MissingPermission")// we have already checked for permission
    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:// user enabled location
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:// user denied location update
                        mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_ENABLE_LOCATION);
                        mGoogleApiClient.disconnect();
                        break;
                }
                break;
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (location != null) {
            mGoogleApiClient.disconnect();
            mLocationProviderCallback.onGetLocation(location.getLatitude() + "", location.getLongitude() + "");
        } else {
            mGoogleApiClient.disconnect();
            mLocationProviderCallback.onLocationError(WeatherFragment.ERROR_GETTING_LOCATION);
        }
    }
}
