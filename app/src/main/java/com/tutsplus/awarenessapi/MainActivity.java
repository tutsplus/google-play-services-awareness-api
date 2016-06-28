package com.tutsplus.awarenessapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        AdapterView.OnItemClickListener{

    private final static int REQUEST_PERMISSION_RESULT_CODE = 42;

    private ListView mListView;
    private String[] mItems;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkLocationPermission();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Awareness.API)
                .enableAutoManage(this, this)
                .build();
        mGoogleApiClient.connect();

    }

    private void initViews() {
        mListView = (ListView) findViewById( R.id.list );
        mItems = getResources().getStringArray(R.array.items);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mItems );
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
    }

    private void detectHeadphones() {
        Awareness.SnapshotApi.getHeadphoneState(mGoogleApiClient)
                .setResultCallback(new ResultCallback<HeadphoneStateResult>() {
                    @Override
                    public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                        HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                        if (headphoneState.getState() == HeadphoneState.PLUGGED_IN) {
                            Log.e("Tuts+", "Headphones are plugged in.");
                        } else {
                            Log.e("Tuts+", "Headphones are NOT plugged in.");
                        }
                    }
                });
    }

    private void detectActivity() {
        Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
                    @Override
                    public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                        ActivityRecognitionResult result = detectedActivityResult.getActivityRecognitionResult();
                        Log.e("Tuts+", "time: " + result.getTime());
                        Log.e("Tuts+", "elapsed time: " + result.getElapsedRealtimeMillis() );
                        Log.e("Tuts+", result.getMostProbableActivity().toString());
                        DetectedActivity activity;
                        for( int i = 0; i < result.getProbableActivities().size(); i++ ) {
                            activity = result.getProbableActivities().get(i);
                            //getType matches up to static values in DetectedActivity
                            Log.e("Tuts+", "Activity: " + activity.getType() + " Liklihood: " + activity.getConfidence() );
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    private void detectLocation() {
        if( !checkLocationPermission() ) {
            return;
        }

        Awareness.SnapshotApi.getLocation(mGoogleApiClient)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        Location location = locationResult.getLocation();
                        Log.e("Tuts+", "Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                    }
                });
    }

    private void detectNearbyPlaces() {
        if( !checkLocationPermission() ) {
            return;
        }

        Awareness.SnapshotApi.getPlaces(mGoogleApiClient)
                .setResultCallback(new ResultCallback<PlacesResult>() {
                    @Override
                    public void onResult(@NonNull PlacesResult placesResult) {
                        List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                        PlaceLikelihood place;
                        for (int i = 0; i < placeLikelihoodList.size(); i++) {
                            place = placeLikelihoodList.get(i);
                            Log.e("Tuts+", place.getPlace().getName().toString() + ", likelihood: " + place.getLikelihood());
                        }
                    }
                });
    }

    private void detectWeather() {
        if( !checkLocationPermission() ) {
            return;
        }

        Awareness.SnapshotApi.getWeather(mGoogleApiClient)
                .setResultCallback(new ResultCallback<WeatherResult>() {
                    @Override
                    public void onResult(@NonNull WeatherResult weatherResult) {
                        Weather weather = weatherResult.getWeather();
                        Log.e("Tuts+", "Temp: " + weather.getTemperature(Weather.FAHRENHEIT));
                        Log.e("Tuts+", "Feels like: " + weather.getFeelsLikeTemperature(Weather.FAHRENHEIT));
                        Log.e("Tuts+", "Dew point: " + weather.getDewPoint(Weather.FAHRENHEIT));
                        Log.e("Tuts+", "Humidity: " + weather.getHumidity() );

                        if( weather.getConditions()[0] == Weather.CONDITION_CLEAR ) {
                            Log.e("Tuts+", "It's clear out!");
                        }
                    }
                });
    }

    private void createFence() {
        
    }

    private boolean checkLocationPermission() {
        if( !hasLocationPermission() ) {
            Log.e("Tuts+", "Does not have location permission granted");
            requestLocationPermission();
            return false;
        }

        return true;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                MainActivity.this,
                new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                REQUEST_PERMISSION_RESULT_CODE );
    }


    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION )
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_RESULT_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    Log.e("Tuts+", "Location permission denied.");
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if( mItems[position].equalsIgnoreCase( getString(R.string.item_snapshot_headphones ) ) ) {
            detectHeadphones();
        } else if( mItems[position].equalsIgnoreCase( getString(R.string.item_snapshot_location ) ) ) {
            detectLocation();
        } else if( mItems[position].equalsIgnoreCase( getString(R.string.item_snapshot_places ) ) ) {
            detectNearbyPlaces();
        } else if( mItems[position].equalsIgnoreCase( getString(R.string.item_snapshot_weather ) ) ) {
            detectWeather();
        } else if( mItems[position].equalsIgnoreCase( getString(R.string.item_fence ) ) ) {
            createFence();
        } else if( mItems[position].equalsIgnoreCase( getString(R.string.item_snapshot_activity))) {
            detectActivity();
        }
    }
}
