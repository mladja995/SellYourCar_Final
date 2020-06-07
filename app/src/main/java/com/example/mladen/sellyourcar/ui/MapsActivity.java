package com.example.mladen.sellyourcar.ui;

import android.Manifest;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.helpers.FirebaseObject;
import com.example.mladen.sellyourcar.helpers.MyClusterManagerRenderer;
import com.example.mladen.sellyourcar.models.Ad;
import com.example.mladen.sellyourcar.models.ClusterMarker;
import com.example.mladen.sellyourcar.models.SingletonUser;
import com.example.mladen.sellyourcar.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>{

    private static final String TAG = "MapsActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final int LOCATION_UPDATE_INTERVAL = 3000;
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private List<User> users;
    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private static ArrayList<ClusterMarker> mClusterMarkersForUsers =  new ArrayList<ClusterMarker>();
    private static ArrayList<ClusterMarker> mClusterMarkersForAds =  new ArrayList<ClusterMarker>();
    private ImageView imageView;
    public static int checkForNumOfUsers = 0;
    public static int checkForNumOfAds = 0;
    public int numOfAdsForAllUsers;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private static boolean showUsersOrAds = true;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FirebaseObject.InitalizeFirebase();

        if(isServicesOK()) {
            getLocationPermission();
        }

        users = new ArrayList<>();
        getAllUsersFromFirestore(users);

        Log.d(TAG, "onCreate: User email: " + SingletonUser.getInstance().user.email);





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: called");
        int id = item.getItemId();

        if(id == R.id.profile_item) {
            Toast.makeText(this, "Show profile", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.show_friends_and_users_item) {
            if(!showUsersOrAds){
                showUsersOrAds = true;
                mClusterManager.clearItems();
                mClusterManager.cluster();
            }
            addMapMarkers();
        }
        else if(id == R.id.show_ads_for_cars_items) {
            if(showUsersOrAds){
                showUsersOrAds = false;
                mClusterManager.clearItems();
                mClusterManager.cluster();
            }
            addMapMarkers();
        }
        else if(id == R.id.logout_item){
            FirebaseObject.mFirebaseAuth.signOut();
            Intent i = new Intent(MapsActivity.this, LoginActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mLocationPermissionsGranted = true;
                initMap();
            }
            else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        Log.d(TAG, "onMapReady: Creating cluster manager");
        mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mMap);

        Log.d(TAG, "onMapReady: Setting listeners to cluster manager and google map");
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterItemInfoWindowClickListener((ClusterManager.OnClusterItemInfoWindowClickListener) this);

        if (mLocationPermissionsGranted)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
        }

    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted)
            {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())
                        {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }
                        else
                        {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapsActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapsActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public void getAllUsersFromFirestore(final List<User> users){
        Log.d(TAG, "getAllUsersFromFirestore: called");
        CollectionReference collectionReference = FirebaseObject.fStore.collection("users");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "getAllUsersFromFirestore: getting users from firestore");
                    for(final QueryDocumentSnapshot document : task.getResult()){
                        DocumentReference documentReference = FirebaseObject.fStore
                                .collection("users")
                                .document(document.getId())
                                .collection("user information")
                                .document("user information");
                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final User user = documentSnapshot.toObject(User.class);
                                //users.add(user);
                                Log.d(TAG, "getAllUsersFromFirestore: " + "getting user with email - " + user.email);
                                DocumentReference adsDocumentReference = FirebaseObject.fStore
                                        .collection("users")
                                        .document(document.getId())
                                        .collection("ads information")
                                        .document("ads information");
                                adsDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot documentSnapshot1 = task.getResult();
                                            if(documentSnapshot1.exists()){
                                                Gson gson = new Gson();
                                                Map<String, Object> data = documentSnapshot1.getData();
                                                String adsJson = gson.toJson(data);
                                                Log.d(TAG, "getAllUsersFromFirestore: Getting ads for user: " + user.email);
                                                Log.d(TAG, "getAllUsersFromFirestore: Ads from user: " + user.email + ", Json file: " + adsJson);
                                                if(!adsJson.equals("{}")){
                                                    int startIndex = adsJson.indexOf("[");
                                                    int endIndex = adsJson.indexOf("]");
                                                    String editedAdsJson = adsJson.substring(startIndex, endIndex + 1);
                                                    Log.d(TAG, "getAllUsersFromFirestore: Ads from user: " + user.email + ", Edited json file: " + editedAdsJson);
                                                    Log.d(TAG, "getAllUsersFromFirestore: Adding list of ads to user " + user.email);
                                                    user.myAds = new Gson().fromJson(
                                                            editedAdsJson, new TypeToken<ArrayList<Ad>>() {}.getType());
                                                    numOfAdsForAllUsers += user.myAds.size();
                                                }
                                            }
                                        }
                                        Log.d(TAG, "getAllUsersFromFirestore: Adding user " + user.email + " to local list of users");
                                        users.add(user);
                                    }
                                });
                            }
                        });
                    }
                }else{
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    private void addMapMarkers() {
        Log.d(TAG, "addMapMarkers: called");
        final ProgressDialog progressDialog = new ProgressDialog(this);
        if(showUsersOrAds) {
            progressDialog.setTitle("Finding users and friends...");
        }else{
            progressDialog.setTitle("Finding car's ads...");
        }
        progressDialog.show();

        if(mMap != null)
        {
            if(mClusterManagerRenderer == null)
            {
                Log.d(TAG, "addMapMarkers: Creating cluster manager renderer and setting it to cluster manager");
                mClusterManagerRenderer = new MyClusterManagerRenderer(getApplicationContext(), mMap, mClusterManager);
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            String snippet = "";
            String avatar;
            if(showUsersOrAds) {
                if (!mClusterMarkersForUsers.isEmpty()) {
                    for (ClusterMarker cm : mClusterMarkersForUsers) {
                        mClusterManager.addItem(cm);
                    }
                    Log.d(TAG, "addMapMarkers: Calling cluster() method on cluster manager");
                    mClusterManager.cluster();
                    progressDialog.dismiss();
                    startUserLocationsRunnable();

                } else {
                    for (User u : users) {
                        try {
                            /*String snippet = "";
                            String avatar;*/
                            if (u.userID.equals(FirebaseObject.mFirebaseAuth.getCurrentUser().getUid())) {
                                snippet = "This is you";
                            } else {
                                snippet = "Determine route to " + u.username + "?";
                            }
                            /*int avatar = R.drawable.cartman_cop; // set the default avatar*/
                            Log.d(TAG, "addMapMarkers: getting user's profile image URL - " + u.profileImgURL);
                            avatar = u.profileImgURL; // set the default avatar
                            LoadImageFromURL loadImageFromURL = new LoadImageFromURL(imageView, u, snippet, progressDialog, null);
                            loadImageFromURL.execute(avatar);
                        } catch (NullPointerException e) {
                            Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                        }

                    }
                }
            }else{
                if(!mClusterMarkersForAds.isEmpty()){
                    for(ClusterMarker cm : mClusterMarkersForAds){
                        mClusterManager.addItem(cm);
                    }
                    Log.d(TAG, "addMapMarkers: Calling cluster() method on cluster manager");
                    mClusterManager.cluster();
                    progressDialog.dismiss();
                }else{
                    for(User u : users){
                        if(u.myAds != null) {
                            for (Ad ad : u.myAds) {
                                try {
                                    snippet = ad.model + " " + ad.mark + " " + ad.year;
                                    Log.d(TAG, "addMapMarkers: getting ad's image URL - " + ad.imageUrl);
                                    avatar = ad.imageUrl;
                                    LoadImageFromURL loadImageFromURL = new LoadImageFromURL(imageView, u, snippet, progressDialog, ad);
                                    loadImageFromURL.execute(avatar);
                                } catch (NullPointerException e) {
                                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onClusterItemInfoWindowClick(ClusterMarker clusterMarker) {
        Log.d(TAG, "onClusterItemClick: called");
        if(showUsersOrAds) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", clusterMarker.getUser());
            Intent intent = new Intent(MapsActivity.this, ProfileView.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }else{
            Toast.makeText(this,"Show car's ad", Toast.LENGTH_LONG).show();
        }


    }

    private class LoadImageFromURL extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private User user;
        private String snippet;
        private ProgressDialog progressDialog;
        private Ad ad;

        public LoadImageFromURL(ImageView mImageView, User u, String s, ProgressDialog pd, Ad ad){
            Log.d(TAG, "LoadImageFromURL: called for user: " + u.email);
            this.imgView = mImageView;
            this.user = u;
            this.snippet = s;
            this.progressDialog = pd;
            this.ad = ad;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            String imageURL = strings[0];
            Bitmap bitmap = null;
            try
            {
                InputStream inputStream = new java.net.URL(imageURL).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute: called");
            ClusterMarker newClusterMarker;
            if(showUsersOrAds) {
                Log.d(TAG, "addMapMarkers: Creating cluster marker for user: " + user.email);
                newClusterMarker = new ClusterMarker(new LatLng(user.cordinates.getLatitude(), user.cordinates.getLongitude()), user.username, snippet, bitmap, user, ad);
                Log.d(TAG, "addMapMarkers: Adding cluster marker to cluster markers list of users");
                mClusterMarkersForUsers.add(newClusterMarker);
            }else{
                Log.d(TAG, "addMapMarkers: Creating cluster marker for ad: " + snippet);
                newClusterMarker = new ClusterMarker(new LatLng(ad.coordinates.getLatitude(), ad.coordinates.getLongitude()), "Car ad", snippet, bitmap, user, ad);
                Log.d(TAG, "addMapMarkers: Adding cluster marker to cluster markers list of ads");
                mClusterMarkersForAds.add(newClusterMarker);
            }

            Log.d(TAG, "addMapMarkers: Adding cluster marker to cluster manager");
            mClusterManager.addItem(newClusterMarker);

            if(showUsersOrAds) {
                checkForNumOfUsers++;
                Log.d(TAG, "onPostExecute: check var: " + checkForNumOfUsers + " users list size: " + users.size());
                if (checkForNumOfUsers == users.size()) {
                    Log.d(TAG, "addMapMarkers: Calling cluster() method on cluster manager");
                    mClusterManager.cluster();
                    checkForNumOfUsers = 0;
                    progressDialog.dismiss();
                    startUserLocationsRunnable();
                }
            }else{
                checkForNumOfAds++;
                Log.d(TAG, "onPostExecute: check var: " + checkForNumOfAds + " number of ads: " + numOfAdsForAllUsers);
                if (checkForNumOfAds == numOfAdsForAllUsers) {
                    Log.d(TAG, "addMapMarkers: Calling cluster() method on cluster manager");
                    mClusterManager.cluster();
                    checkForNumOfAds = 0;
                    progressDialog.dismiss();

                }
            }
        }
    }

    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                retrieveUserLocations();
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void retrieveUserLocations(){
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users in the chatroom.");

        try{
            for(final ClusterMarker clusterMarker: mClusterMarkersForUsers){

                DocumentReference documentReference = FirebaseObject.fStore.collection("users")
                        .document(clusterMarker.getUser().userID).collection("user information").document("user information");

                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){

                            final User user = task.getResult().toObject(User.class);

                            // update the location
                            for (int i = 0; i < mClusterMarkersForUsers.size(); i++) {
                                try {
                                    if (mClusterMarkersForUsers.get(i).getUser().userID.equals(user.userID)) {

                                        LatLng updatedLatLng = new LatLng(user.cordinates.getLatitude(), user.cordinates.getLongitude());

                                        mClusterMarkersForUsers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkersForUsers.get(i));
                                    }


                                } catch (NullPointerException e) {
                                    Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*startUserLocationsRunnable();*/ // update user locations every 'LOCATION_UPDATE_INTERVAL'
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates(); // stop updating user locations
    }
}
