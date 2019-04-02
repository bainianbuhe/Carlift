package osu.carlift;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

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
    JSONObject locationJSONObject;
    private String userName;
    private String startAddr;
    private String destinationAddr;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

        /*if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }*/

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
        /*mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);*/

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
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        title = mSearchText.getText().toString();
        Log.d(TAG,"searchString is "+title);
        GetLocationDownloadTask getLocationDownloadTask=new GetLocationDownloadTask();
       // Log.d(TAG,"address got is "+locationJSONObject.toString());
        String searchString="https://maps.googleapis.com/maps/api/geocode/json?address="+title+"&key=AIzaSyA_UcjRk9RCmhLG4YvrCgJT8qNPplZwlZ0";
        searchString=searchString.replaceAll(" ","+");
        getLocationDownloadTask.execute(searchString);
        getLocationDownloadTask.completionCode = new GetLocationDownloadTask.AsyncIfc() {

            @Override
            public void onComplete(JSONObject jsonObject) {
           locationJSONObject=jsonObject;
           Log.d(TAG,"the returned jSONObject is "+locationJSONObject);
               try {
                   double lat = locationJSONObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                   double lng = locationJSONObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                   if (locationJSONObject!=null){
                       Toast.makeText(SelectLocationActivity.this,"Successfully finding the searched place",Toast.LENGTH_SHORT).show();
                   moveCamera(new LatLng(lat, lng),DEFAULT_ZOOM, title);
                  }
                   else{
                       Toast.makeText(SelectLocationActivity.this,"Cannot find the place you search!",Toast.LENGTH_SHORT).show();
                   }
               }
               catch (JSONException e)
               {}
            }
        };
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


}


