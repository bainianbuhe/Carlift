package osu.carlift;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Arrays;


public class SeePlaceInMapActivity extends AppCompatActivity implements OnMapReadyCallback,AsyncResponse,GoogleApiClient.OnConnectionFailedListener {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String TAG = "SeePlaceInMapActivity";
    private static final String EXTRA_USERNAME="userName";
    private static final String EXTRA_START_ADDR="start_addr";
    private static final String EXTRA_DESTINATION="destination";
    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;
    JSONObject locationJSONObject;
    JSONObject infoJSONObject;
    private String userName;
    private String startAddr;
    private String destinationAddr;
    private PlaceInfo mPlace;
    private String placeId;
    private static Context context;
    private PlaceInfo placeInfo;
    //widgets
    private Button mSeeStartingPlace;
    private Button mSeeDestination;
    private ImageView mWebSite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context=getApplicationContext();
        setContentView(R.layout.activity_see_place_in_map);
        getLocationPermission();
        startAddr = getIntent().getStringExtra(EXTRA_START_ADDR);
        destinationAddr=getIntent().getStringExtra(EXTRA_DESTINATION);
        mSeeDestination=(Button)findViewById(R.id.see_destination_button);
        mSeeDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoLocate(destinationAddr);
            }
        });
        mSeeStartingPlace=(Button)findViewById(R.id.see_starting_place_button);
        mSeeStartingPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                geoLocate(startAddr);
            }
        });
        mWebSite=findViewById(R.id.go_to_website_driver);
        mWebSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeInfo==null)
                {Toast.makeText(SeePlaceInMapActivity.this,"Please click on one of the two buttons below first!",Toast.LENGTH_SHORT).show();
                }
                else{
                if (placeInfo.getWebsiteUri() != null) {
                    Intent intent=new Intent(SeePlaceInMapActivity.this,WebViewActivity.class);
                    Log.d(TAG,"uri here"+placeInfo.getWebsiteUri());
                    intent.putExtra("url",placeInfo.getWebsiteUri().toString());
                    startActivity(intent);
                } else {
                }
            }
            }
        });



    }



    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "map is ready");
        Toast.makeText(SeePlaceInMapActivity.this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }
    private void getDeviceLocation(){
        Log.d(TAG,"getting device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()&&task.getResult()!=null){
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");
                        }else{
                            Toast.makeText(SeePlaceInMapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG,"error"+e);
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }
    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(SeePlaceInMapActivity.this));
        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }
    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initMap(){
        Log.d(TAG,"initing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(SeePlaceInMapActivity.this);
    }
    private void geoLocate(String title) {
        Log.d(TAG, "geoLocate: geolocating");

        Log.d(TAG, "searchString is " + title);
        GetLocationDownloadTaskDriver getLocationDownloadTaskDriver = new GetLocationDownloadTaskDriver();
        // Log.d(TAG,"address got is "+locationJSONObject.toString());
        String searchString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + title + "&key=AIzaSyA_UcjRk9RCmhLG4YvrCgJT8qNPplZwlZ0";
        searchString = searchString.replaceAll(" ", "+");
        getLocationDownloadTaskDriver.execute(searchString);

        getLocationDownloadTaskDriver.completionCode = new GetLocationDownloadTaskDriver.AsyncIfc() {

            @Override
            public void onComplete(PlaceInfo place_info)
            {
                placeInfo=place_info;
                Log.d(TAG,"placeinfo is"+place_info.toString());
                moveCamera(place_info.getLatlng(),DEFAULT_ZOOM,place_info);
            }};


    }
    @Override
    public void processFinish(JSONObject output) {
        Log.d(TAG,"processfinish");
        locationJSONObject=output;
        Log.d(TAG,"address got is "+output.toString());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public static Context getContext(){
        return context;
    }



}

