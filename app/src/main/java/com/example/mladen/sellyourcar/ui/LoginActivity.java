package com.example.mladen.sellyourcar.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mladen.sellyourcar.helpers.FirebaseObject;
import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.models.Ad;
import com.example.mladen.sellyourcar.models.SingletonUser;
import com.example.mladen.sellyourcar.models.User;
import com.example.mladen.sellyourcar.services.LocationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener{


    /*Variables*/
    private static final String TAG = "LoginActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private EditText emailId, password;
    private Button btnSignIn;
    private TextView tvSignUp;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*Initialize*/
        FirebaseObject.InitalizeFirebase();
        bundle = new Bundle();

        /*Getting layout's components*/
        emailId = findViewById(R.id.login_edit_email);
        password = findViewById(R.id.login_edit_password);
        btnSignIn = findViewById(R.id.login_btn_signin);
        tvSignUp = findViewById(R.id.login_label);

        /*Setting listeners*/
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String email = emailId.getText().toString();
                String pwd = password.getText().toString();
                if(email.isEmpty() && pwd.isEmpty())
                {
                    Toast.makeText(LoginActivity.this, "Fields Are Empty!", Toast.LENGTH_SHORT).show();
                }
                else if(email.isEmpty())
                {
                    emailId.setError("Please enter email id");
                    emailId.requestFocus();

                }
                else if(pwd.isEmpty())
                {
                    password.setError("Please enter your password");
                    password.requestFocus();
                }

                else if (!(email.isEmpty() && pwd.isEmpty()))
                {
                    FirebaseObject.mFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(LoginActivity.this, "Login Error, Please Login Again", Toast.LENGTH_SHORT).show();
                            }
                            else { }
                        }
                    });
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Error Occurred!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
                if(bundle != null) {
                    i.putExtras(bundle);
                }
                startActivity(i);
            }
        });

    }

   @Override
    protected void onStart()
    {
        Log.d(TAG, "onStart: started");
        super.onStart();
        FirebaseObject.mFirebaseAuth.addAuthStateListener(this);
        if(isServicesOK()) {
            getLocationPermission();

        }
    }

    @Override
    protected void onStop()
    {
        Log.d(TAG, "onStop: started");
        super.onStop();
        FirebaseObject.mFirebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.d(TAG, "onAuthStateChanged: called");
        final FirebaseUser mFirebaseUser = FirebaseObject.mFirebaseAuth.getCurrentUser();
        if(mFirebaseUser != null)
        {
            Toast.makeText(LoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
            DocumentReference userDocumentReference = FirebaseObject.fStore.collection("users")
                    .document(mFirebaseUser.getUid()).collection("user information")
                    .document("user information");
            userDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    User userInformation = documentSnapshot.toObject(User.class);
                    final SingletonUser singletonUser = SingletonUser.getInstance();
                    singletonUser.user = userInformation;
                    Log.d(TAG, "onAuthStateChanged: User logged with email: " + singletonUser.user.email);
                    
                    DocumentReference adsDocumentReference = FirebaseObject.fStore.collection("users")
                            .document(mFirebaseUser.getUid())
                            .collection("ads information")
                            .document("ads information");

                    adsDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                DocumentSnapshot document = task.getResult();
                                if(document.exists())
                                {
                                    Gson gson = new Gson();
                                    Map<String, Object> data = document.getData();
                                    String adsJson = gson.toJson(data);
                                    Log.d(TAG, "onAuthStateChanged: Ads from user: " + singletonUser.user.email + ", Json file: " + adsJson);
                                    if(!adsJson.equals("{}"))
                                    {
                                        int startIndex = adsJson.indexOf("[");
                                        int endIndex = adsJson.indexOf("]");
                                        String editedAdsJson = adsJson.substring(startIndex, endIndex + 1);
                                        Log.d(TAG, "onAuthStateChanged: Ads from user: " + singletonUser.user.email + ", Edited json file: " + editedAdsJson);
                                        singletonUser.myAds = new Gson().fromJson(
                                                editedAdsJson, new TypeToken<ArrayList<Ad>>() {}.getType()
                                        );

                                    }
                                }
                                Intent i = new Intent(LoginActivity.this, NearestVehiclesActivity.class);
                                startActivity(i);
                            }
                        }}).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onAuthStateChanged: onFailure: List of ads is empty or some error occurred - " + e.getMessage());
                        }});
                }
            });

        }
        else
        {
            Toast.makeText(LoginActivity.this, "Please login", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                getDeviceLocation(bundle);


            }
            else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            break;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    getDeviceLocation(bundle);

                    //initialize our map

                }
            }
        }
    }

    private void getDeviceLocation(final Bundle bundle) {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionsGranted) {

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            bundle.putDouble("latitude", currentLocation.getLatitude());
                            bundle.putDouble("longitude", currentLocation.getLongitude());
                            startLocationService();
                        }
                        else
                        {
                            Log.d(TAG, "onComplete: current location is null");

                        }
                    }
                });
            }
        }
        catch (SecurityException e)
        {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);
            //this.startService(serviceIntent);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                LoginActivity.this.startForegroundService(serviceIntent);
            }
            else
            {
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.mladen.sellyourcar.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }
}


