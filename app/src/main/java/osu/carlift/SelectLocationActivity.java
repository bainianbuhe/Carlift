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


public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback,AsyncResponse,GoogleApiClient.OnConnectionFailedListener {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String TAG = "SelectLocationActivity";
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
    private AutoCompleteTextView mSearchText;
    private double latitudeCur;
    private double latitudeDes;
    private Button mConfirmStartingPlace;
    private Button mConfirmDestination;
    private Button mConfirmAllSet;
    private double longitudeCur;
    private double longitudeDes;
    private double dis;
    private String title;
    private ImageView mWebSite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context=getApplicationContext();
        setContentView(R.layout.activity_select_location);
        getLocationPermission();
        //String s = ((MyApplication) getApplication()).getLocation();
        userName=getIntent().getStringExtra(EXTRA_USERNAME);
        String s = getIntent().getStringExtra("Get_Location");
        mSearchText=(AutoCompleteTextView) findViewById(R.id.input_search);
        mSearchText.setMaxLines(1);
        mSearchText.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchText.setText(s);
        mConfirmStartingPlace=(Button)findViewById(R.id.confirm_starting_point_button);
        mConfirmDestination=(Button)findViewById(R.id.confirm_destination_button);
        mConfirmAllSet=(Button)findViewById(R.id.confirm_all_done);
        mConfirmStartingPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddr=title;
            }
        });
        mConfirmDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationAddr=title;
            }
        });
        mConfirmAllSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SelectLocationActivity.this,UserAddRequest.class);
                intent.putExtra(EXTRA_USERNAME,userName);
                intent.putExtra(EXTRA_START_ADDR,startAddr);
                intent.putExtra(EXTRA_DESTINATION,destinationAddr);
                finish();
                startActivity(intent);
            }
        });
        mWebSite=findViewById(R.id.go_to_website);
        mWebSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeInfo==null)
                {Toast.makeText(SelectLocationActivity.this,"Please click on one of the two buttons below first!",Toast.LENGTH_SHORT).show();}
                else{
                if (placeInfo.getWebsiteUri() != null) {
                    Intent intent=new Intent(SelectLocationActivity.this,WebViewActivity.class);
                    Log.d(TAG,"uri here"+placeInfo.getWebsiteUri());
                    intent.putExtra("url",placeInfo.getWebsiteUri().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(SelectLocationActivity.this, "you have not searched for a place yet or cannot find website for the current place",
                            Toast.LENGTH_SHORT).show();
                }
            }}
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
        Toast.makeText(SelectLocationActivity.this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
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
                                latitudeCur = currentLocation.getLatitude();
                                longitudeCur = currentLocation.getLongitude();
                        }else{
                            Toast.makeText(SelectLocationActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
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
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(SelectLocationActivity.this));
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

        mapFragment.getMapAsync(SelectLocationActivity.this);
    }
    private void init(){
        Log.d(TAG,"initializing searching bar");
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();



        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });
    }
    private void geoLocate() {
        Log.d(TAG, "geoLocate: geolocating");

        title = mSearchText.getText().toString();
        Log.d(TAG, "searchString is " + title);
        GetLocationDownloadTask getLocationDownloadTask = new GetLocationDownloadTask();
        // Log.d(TAG,"address got is "+locationJSONObject.toString());
        String searchString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + title + "&key=AIzaSyA_UcjRk9RCmhLG4YvrCgJT8qNPplZwlZ0";
        searchString = searchString.replaceAll(" ", "+");
        getLocationDownloadTask.execute(searchString);

        getLocationDownloadTask.completionCode = new GetLocationDownloadTask.AsyncIfc() {

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


