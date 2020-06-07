package com.example.mladen.sellyourcar.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.helpers.FirebaseObject;
import com.example.mladen.sellyourcar.models.Ad;
import com.example.mladen.sellyourcar.models.SingletonUser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostAnAdActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostAnAdActivity";
    private static final int PICK_IMAGE_REQUEST = 22;

    private EditText etDescription, etMark, etModel, etYear, etMileage, etFuel;
    private Button postAdOnCurrentLocation;
    private ImageView imgAd;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Uri filePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_an_ad);
        Log.d(TAG, "onCreate: called");

        /*Initialize*/
        FirebaseObject.InitalizeFirebase();

        /*Getting layout's components*/
        postAdOnCurrentLocation = findViewById(R.id.PostAnAd_btnPostAdOnCurrentLocation);
        etDescription = findViewById(R.id.PostAnAd_etDescription);
        etMark = findViewById(R.id.PostAnAd_etMark);
        etModel = findViewById(R.id.PostAnAd_etModel);
        etYear = findViewById(R.id.PostAnAd_etYear);
        etMileage = findViewById(R.id.PostAnAd_etMileage);
        etFuel = findViewById(R.id.PostAnAd_etFuel);
        imgAd = findViewById(R.id.PostAnAd_imgAd);

        /*Setting listeners*/
        postAdOnCurrentLocation.setOnClickListener(this);
        imgAd.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.PostAnAd_btnPostAdOnCurrentLocation: {
                final String description = etDescription.getText().toString();
                final String mark = etMark.getText().toString();
                final String model = etModel.getText().toString();
                final String year = etYear.getText().toString();
                final String mileage = etMileage.getText().toString();
                final String fuel = etFuel.getText().toString();

                if ((description.isEmpty() && mark.isEmpty() && model.isEmpty() && year.isEmpty() && mileage.isEmpty() && fuel.isEmpty()) || (filePath == null)) {
                    Toast.makeText(PostAnAdActivity.this, "Fill in all the fields and choose photo for your ad!", Toast.LENGTH_SHORT).show();
                } else if (mark.isEmpty()) {
                    etMark.setError("Please enter mark");
                    etMark.requestFocus();

                } else if (model.isEmpty()) {
                    etModel.setError("Please enter model");
                    etModel.requestFocus();

                } else if (year.isEmpty()) {
                    etYear.setError("Please enter year");
                    etYear.requestFocus();

                } else if (mileage.isEmpty()) {
                    etMileage.setError("Please enter mileage");
                    etMileage.requestFocus();

                } else if (fuel.isEmpty()) {
                    etDescription.setError("Please enter fuel");
                    etDescription.requestFocus();

                } else if (description.isEmpty()) {
                    etDescription.setError("Please enter description");
                    etDescription.requestFocus();

                } else {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                    final Task location = mFusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                Log.d(TAG, "onComplete: found location!");
                                Location location = (Location) task.getResult();
                                uploadImage(description, mark, model, year, mileage, fuel, new GeoPoint(location.getLatitude(), location.getLongitude()));
                            }
                            else
                            {
                                Log.d(TAG, "onComplete: current location is null");
                            }
                        }
                    });
                }
                break;
            }

            case R.id.PostAnAd_imgAd:
            {
                selectImage();
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_post_an_ad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.show_map_item)
        {
            Toast.makeText(this, "show_map_item!", Toast.LENGTH_SHORT).show();
        }

        else if(id == R.id.profile_item)
        {
            Toast.makeText(this, "profile_item!", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.message_item)
        {
            /*Toast.makeText(this, "message_item!", Toast.LENGTH_SHORT).show();*/
            Intent intent = new Intent(PostAnAdActivity.this, StartActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void SaveAdToFirebase(final String imageUrl, final String description, final String mark,
                                 final String model, final String year, final String mileage,
                                 final String fuel, final GeoPoint coordinates)
    {
        Log.d(TAG, "SaveAdToFirebase: called");
        Gson gson = new Gson();
        Ad ad = new Ad(imageUrl, description, mark, model, year, mileage, fuel, coordinates);
        SingletonUser.getInstance().myAds.add(ad);
        String adsJson = gson.toJson(SingletonUser.getInstance().myAds);
        String newJson = "{myList:" + adsJson + "}";

        Log.d(TAG, "SaveAdToFirebase: JSON list of ads: " + adsJson);
        Log.d(TAG, "SaveAdToFirebase: JSON list of ads edited: " + newJson);
        Map<String, Object> data = new Gson().fromJson(
                newJson, new TypeToken<HashMap<String, Object>>() {}.getType()
        );

        DocumentReference documentReferenceAds = FirebaseObject.fStore.collection("users")
                .document(FirebaseObject.mFirebaseAuth.getCurrentUser().getUid())
                .collection("ads information").document("ads information");

        documentReferenceAds.set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(PostAnAdActivity.this, "Ad created.",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostAnAdActivity.this,
                        "Ad went wrong, Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imgAd.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final String description, final String mark, final String model,
                             final String year, final String mileage, final String fuel,
                             final GeoPoint coordinates)
    {
        if (filePath != null)
        {
            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            int numOfAds = SingletonUser.getInstance().myAds.size();
            final StorageReference imageReference = FirebaseObject.storageReference
                    .child(FirebaseObject.mFirebaseAuth.getUid())
                    .child("Images").child("Ad " + String.valueOf(numOfAds));
            // adding listeners on upload
            // or failure of image
            imageReference.putFile(filePath).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            String imageURL = uri.toString();
                            progressDialog.dismiss();
                            Toast.makeText(PostAnAdActivity.this, "Image Uploaded!",
                                    Toast.LENGTH_SHORT).show();
                            SaveAdToFirebase(imageURL, description, mark, model, year, mileage,
                                    fuel, coordinates);
                        }

                    });
                    // Image uploaded successfully
                    // Dismiss dialog
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    // Error, Image not uploaded
                    progressDialog.dismiss();
                    Toast.makeText(PostAnAdActivity.this, "Failed " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                // Progress Listener for loading
                // percentage on the dialog box
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() /
                            taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int)progress + "%");
                }
            });
        }
    }

}
