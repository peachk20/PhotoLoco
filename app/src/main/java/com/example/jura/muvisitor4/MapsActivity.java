package com.example.jura.muvisitor4;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jura.muvisitor4.objects.RouteNode;
import com.example.jura.muvisitor4.services.MUVisitorDBAdapter;
import com.example.jura.muvisitor4.util.ProjectConstant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    //Saved Instance Constant
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "request_location_update";
    private static final String LOCATION_KEY = "last_location";
    private static final String LAST_UPDATED_TIME_STRING_KEY = "last_update_time";
    private static final String ROUTE_NODE_KEY = "route_node";
    private static final String ROUTE_NODE_NUMBER_KEY = "node_number";


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private MUVisitorDBAdapter dbAdapter = new MUVisitorDBAdapter(this);
    private RouteNode node;
    private int nodeNumber;
    private final int LOCATION_INTERVAL = 1000;
    private final int LOCATION_FAST_INTERVAL = 500;
    private GoogleApiClient  mGoogleApiClient;
    private boolean mapReady;

    //New Location API
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates = true;
    private boolean mLocationUpdateReady = false;
    private String mLastUpdateTime;

    //Geofence API
    private ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();
    private final float GEOFENCE_RADIUS_IN_METERS = 50;
    private PendingIntent  mGeofencePendingIntent;
    private boolean inTheArea;

    //UI
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mInTheArea;
    private ImageButton btnLeft;
    private ImageButton btnRight;
    private ImageButton btnCamera;
    private ImageButton btnGallery;

    //Camera
    private Uri fileUri;
    private static String uriPath;
    private ImageView imgDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onToast("Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //UI
        mLatitudeText = (TextView) findViewById(R.id.text_lat);
        mLongitudeText = (TextView) findViewById(R.id.text_lng);
        mInTheArea = (TextView) findViewById(R.id.text_in_area);

        btnLeft = (ImageButton) findViewById(R.id.btn_left);
        btnRight = (ImageButton) findViewById(R.id.btn_right);
        btnCamera = (ImageButton) findViewById(R.id.btn_camera);
        btnGallery = (ImageButton) findViewById(R.id.btn_gallery);

        btnLeft.setOnClickListener(btnLeftListener);
        btnRight.setOnClickListener(btnRightListener);
        btnCamera.setOnClickListener(btnCameraListener);
        btnGallery.setOnClickListener(btnGalleryListener);


        //flag
        mapReady = isMapReady();



        Intent intent = getIntent();
        if(mapReady){
            //DB
            dbAdapter.open();
            nodeNumber = 1;

            int tempNodeId;
            if(intent.hasExtra(MainActivity.NODE_ID_INTENT)&&
                    intent.hasExtra(MainActivity.NODE_NUMBER_INTENT)){

                tempNodeId = Integer.parseInt(intent.getStringExtra(MainActivity.NODE_ID_INTENT));
                this.nodeNumber = Integer.parseInt(intent.getStringExtra(MainActivity.NODE_NUMBER_INTENT));
                node = dbAdapter.getNodeById(tempNodeId);
            }
            else{
                int routeId = Integer.parseInt(intent.getStringExtra(MainActivity.ROUTE_ID_INTENT));
                node = dbAdapter.getStartNode(routeId);
            }

        }

        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    protected void onStart() {
        onToast("Start");
        super.onStart();  // Always call the superclass method first

        //Location API
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

        // Geofence init
        IntentFilter mStatusIntentFilter = new IntentFilter(
                ProjectConstant.BROADCAST_ACTION);
        GeofenceResponseReceiver grr =new GeofenceResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                grr,
                mStatusIntentFilter);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        onToast("RestoreInstanceState");
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
//        updateValuesFromBundle(savedInstanceState);
    }

    @Override
    protected void onResume() {
        onToast("Resume");
        super.onResume();
        if(mapReady){
            setUpMap(node);
        }
        //In case that resume from onPause, start loc update
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        updateArrowBtnStatus();
    }



    @Override
    protected void onPause() {
        onToast("Pause");
        super.onPause();

        //stop to save battery
        stopLocationUpdates();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        onToast("Connected");
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        onToast("LocationChanged");
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        //update lat and lng value on screen
        updateUI();

        //location changed, so change flag to ready and init Geofence
        if(!mLocationUpdateReady){
            mLocationUpdateReady = true;
            geofenceByNode(this.node);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        onToast("ConnectionSuspended");
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        onToast("ConnectionFailed");
    }
    @Override
    public void onResult(Status status) {
//        onToast("Result");
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        onToast("SaveInstance");
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mLastLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);

        savedInstanceState.putParcelable(ROUTE_NODE_KEY, node);
        savedInstanceState.putInt(ROUTE_NODE_NUMBER_KEY, nodeNumber);
        super.onSaveInstanceState(savedInstanceState);
    }

    //Checking whether ready to init map or not
    private boolean isMapReady() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                return true;
            }
        }
        return false;
    }

    //set up Google Map
    private void setUpMap(RouteNode node) {
        mMap.clear();
        mMap.setMyLocationEnabled(true);
        Resources res = getResources();
        int zoomAmount = res.getInteger(R.integer.MAP_ZOOM);
        int bearingAmount = res.getInteger(R.integer.MAP_BEARING);
        int tiltAmount = res.getInteger(R.integer.MAP_TILT);
        LatLng location = new LatLng(node.lat, node.lng);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to Mountain View
                .zoom(zoomAmount)                   // Sets the zoom
                .bearing(bearingAmount)                // Sets the orientation of the camera to east
                .tilt(tiltAmount)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .position(location)
                        .title(nodeNumber+"." +node.nodeName)
        );
        marker.showInfoWindow();

    }



    //Connect to Google Play Service
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //create location request
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //start loc update
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        geofenceByNode(this.node);
    }

    //update lat and lng value shown on screen
    private void updateUI() {
        if(mLastLocation != null){
            mLatitudeText.setText("Lat: " + String.valueOf(mLastLocation.getLatitude()) + " ");
            mLongitudeText.setText("Lng: " + String.valueOf(mLastLocation.getLongitude()) + " ");
        }
    }

    //stop loc update
    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mLocationUpdateReady = false;
        }
    }

    //use to store saved instance state
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
//                setButtonsEnabledState();
            }
            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocation is not null.
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }

            if (savedInstanceState.keySet().contains(ROUTE_NODE_NUMBER_KEY)){
                this.nodeNumber = savedInstanceState.getInt(ROUTE_NODE_NUMBER_KEY);
                if (savedInstanceState.keySet().contains(ROUTE_NODE_KEY)){
                    this.node = savedInstanceState.getParcelable(ROUTE_NODE_KEY);
                }
                else
                    this.nodeNumber = 1;
            }

            updateUI();
        }
    }

    //get geofencingRequest
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    //get intent for geofence service
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra(MainActivity.ROUTE_ID_INTENT, node.routeId);
        intent.putExtra(MainActivity.NODE_ID_INTENT, node.nodeId);
        intent.putExtra(MainActivity.NODE_NUMBER_INTENT, nodeNumber);
        // We use FLAG_UPDATE_CURRENT so that we get the same pe4nding intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }
    private void beforeGeofence(){
        inTheArea = false;
        updateInAreaUIStatus();
    }

    //clear previous geofence and then add new one instead (change node, so new geofence)
    private void geofenceByNode(RouteNode node){
        beforeGeofence();
        if (mGoogleApiClient.isConnected() && mLocationUpdateReady){

            //Clear things out first
            mGeofenceList.clear();
            if (mGeofencePendingIntent != null){
                LocationServices.GeofencingApi.removeGeofences(
                        mGoogleApiClient,
                        getGeofencePendingIntent()
                ).setResultCallback(this);
            }

            //Geofence from RouteNode
            mGeofenceList.add(new Geofence.Builder()
                    .setRequestId(node.nodeName)

                    .setCircularRegion(
                            node.lat,
                            node.lng,
                            GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
    }

    protected void onToast(String s){
//        Toast.makeText(getBaseContext(), "on "+s, Toast.LENGTH_SHORT).show();
    }
    protected void toast(String s){
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }

    //go to previous node
    public void toPrevNode(){
        if(node.nodePrev != 0){
            nodeNumber--;
            node= dbAdapter.getNodeById(node.nodePrev);
            setUpMap(node);
            updateArrowBtnStatus();
            geofenceByNode(node);
        }
    }

    //go to next node
    public void toNextNode(){
        if(node.nodeNext != 0){
            nodeNumber++;
            node= dbAdapter.getNodeById(node.nodeNext);
            setUpMap(node);
            updateArrowBtnStatus();
            geofenceByNode(node);
        }
    }

    //enable/disable button
    private void updateArrowBtnStatus() {
        if(node.nodePrev == 0)
            btnLeft.setEnabled(false);
        else
            btnLeft.setEnabled(true);

        if(node.nodeNext == 0)
            btnRight.setEnabled(false);
        else
            btnRight.setEnabled(true);
    }

    //update wheter in the area of geofence or not, toogle camera btn
    private void updateInAreaUIStatus(){
        if(inTheArea){
            mInTheArea.setText("In the area.");
            btnCamera.setEnabled(true);
        }
        else{
            mInTheArea.setText("Out of the area.");
            btnCamera.setEnabled(false);
        }
    }

    private View.OnClickListener btnRightListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            toNextNode();
        }
    };
    private View.OnClickListener btnLeftListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            toPrevNode();
        }
    };

    //to camera activity
    private View.OnClickListener btnCameraListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(ProjectConstant.MEDIA_TYPE_IMAGE); // create a file to save the image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

            // start the image capture Intent
            startActivityForResult(intent, ProjectConstant.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    };

    //to gallery
    private View.OnClickListener btnGalleryListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getBaseContext(), GalleryActivity.class);
            intent.putExtra(MainActivity.ROUTE_ID_INTENT, node.routeId);
            startActivity(intent);
        }
    };


    //recieve result from camera activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ProjectConstant.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                File imgFile = new  File(uriPath);

                if(imgFile.exists()){
                    toast("Image saved to: " + uriPath);
                    dbAdapter.addPhoto(imgFile.getAbsolutePath(),node.nodeId);
                    dbAdapter.updateRouteStatus(node.routeId);
                }

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

    }

    // Broadcast receiver for receiving status updates from the IntentService
    private class GeofenceResponseReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private GeofenceResponseReceiver() {
        }
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            onToast("Receive (GeofenceResponseReceiver)");
            inTheArea = intent.getBooleanExtra(ProjectConstant.EXTENDED_DATA_STATUS,true);
            updateInAreaUIStatus();
        }
    }

    //File URI
    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == ProjectConstant.MEDIA_TYPE_IMAGE){
            uriPath = mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg";
            mediaFile = new File(uriPath);
        } else {
            return null;
        }

        return mediaFile;
    }
}
