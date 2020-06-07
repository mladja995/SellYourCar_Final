package com.example.mladen.sellyourcar.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.models.User;

import java.io.IOException;
import java.io.InputStream;

public class ProfileView extends AppCompatActivity {

    private static final String TAG = "ProfileView";
    private TextView txtName, txtSurname, txtUsername, txtEmail, txtNumber;
    private ImageView imgvProfileImage;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        /*Getting layout's components*/
        txtName = findViewById(R.id.profileView_txtName);
        txtSurname = findViewById(R.id.profileView_txtSurname);
        txtUsername = findViewById(R.id.profileView_txtUsername);
        txtEmail = findViewById(R.id.profileView_txtEmail);
        txtNumber = findViewById(R.id.profileView_txtContactNumber);
        imgvProfileImage = findViewById(R.id.profileView_imgvProfileImage);

        Log.d(TAG, "onCreate: Getting bundle from MapsActivity");
        Bundle bundle = getIntent().getExtras();

        Log.d(TAG, "onCreate: Getting user from bundle");
        user = bundle.getParcelable("user");

        LoadImageFromURL loadImageFromURL = new LoadImageFromURL(imgvProfileImage);
        loadImageFromURL.execute(user.profileImgURL);
    }

    private class LoadImageFromURL extends AsyncTask<String, Void, Bitmap>
    {
        private ImageView imgView;


        public LoadImageFromURL(ImageView mImageView){
            this.imgView = mImageView;
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
            /*Setting user information to layout's components*/
            imgView.setImageBitmap(bitmap);
            txtName.setText(user.name);
            txtSurname.setText(user.surname);
            txtUsername.setText(user.username);
            txtEmail.setText(user.email);
            txtNumber.setText(user.number);
        }
    }

}
