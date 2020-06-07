package com.example.mladen.sellyourcar.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mladen.sellyourcar.helpers.FirebaseObject;
import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.models.SingletonUser;
import com.example.mladen.sellyourcar.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class RegistrationActivity extends AppCompatActivity
{



    /*Variables*/
    private static final String TAG = "RegistrationActivity" ;
    private static final int PICK_IMAGE_REQUEST = 22;
    private EditText etEmail, etPassword, etName, etSurname, etUsername, etNumber;
    private Button btnSignUp, btnUploadImg;
    private TextView tvSignIn;
    private ImageView profileImgView;
    private String userID;
    private Uri filePath;
    private double latitude;
    private double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Initalize*/
        FirebaseObject.InitalizeFirebase();

        final Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            latitude = bundle.getDouble("latitude");
            longitude = bundle.getDouble("longitude");
        }

        /*Getting layout's components*/
        etName = findViewById(R.id.main_edit_name);
        etSurname = findViewById(R.id.main_edit_surname);
        etUsername = findViewById(R.id.main_edit_username);
        etNumber = findViewById(R.id.main_edit_number);
        etEmail = findViewById(R.id.main_edit_email);
        etPassword = findViewById(R.id.main_edit_password);
        btnSignUp = findViewById(R.id.main_btn_signup);
        btnUploadImg = findViewById(R.id.main_btn_uploadimg);
        tvSignIn = findViewById(R.id.main_label);
        profileImgView = findViewById(R.id.main_img_profile);


        /*Setting listeners*/
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = etName.getText().toString();
                final String surname = etSurname.getText().toString();
                final String username = etUsername.getText().toString();
                final String number = etNumber.getText().toString();
                final String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if(email.isEmpty() && password.isEmpty() && name.isEmpty() && surname.isEmpty() && username.isEmpty() && number.isEmpty())
                {
                    Toast.makeText(RegistrationActivity.this, "Fields Are Empty!", Toast.LENGTH_SHORT).show();
                }
                else if(name.isEmpty())
                {
                    etName.setError("Please enter name");
                    etName.requestFocus();
                }
                else if(surname.isEmpty())
                {
                    etSurname.setError("Please enter surname");
                    etSurname.requestFocus();
                }
                else if(username.isEmpty())
                {
                    etUsername.setError("Please enter username");
                    etUsername.requestFocus();
                }
                else if(number.isEmpty())
                {
                    etNumber.setError("Please enter phone number");
                    etNumber.requestFocus();
                }
                else if(email.isEmpty())
                {
                    etEmail.setError("Please enter email");
                    etEmail.requestFocus();
                }
                else if(password.isEmpty())
                {
                    etPassword.setError("Please enter your password");
                    etPassword.requestFocus();
                }

                else if (!(email.isEmpty() && password.isEmpty() && name.isEmpty() && surname.isEmpty() && username.isEmpty() && number.isEmpty()))
                {
                    FirebaseObject.mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                userID = FirebaseObject.mFirebaseAuth.getCurrentUser().getUid();
                                uploadImage(userID, username, email, name, surname, number);
                            }
                            else
                            {
                                Toast.makeText(RegistrationActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }


            }
        });

        tvSignIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });

        profileImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });




    }



    private void selectImage() {
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                profileImgView.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final String userID, final String username, final String email, final String name, final String surname, final String number) {
        if (filePath != null)
        {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference imageReference = FirebaseObject.storageReference.child(FirebaseObject.mFirebaseAuth.getUid()).child("Images").child("Profile Pic");
            // adding listeners on upload
            // or failure of image

            imageReference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
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
                            Toast.makeText(RegistrationActivity.this, "Image Uploaded!", Toast.LENGTH_SHORT).show();
                            SaveUserInformation(userID, username, email, name, surname, number, imageURL);

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
                            Toast.makeText(RegistrationActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        // Progress Listener for loading
                        // percentage on the dialog box
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int)progress + "%");
                        }
            });
        }
    }

    public void SaveUserInformation(final String userID, String username, String email, String name, String surname, String number, String profileImgURL) {
        Log.d(TAG, "SaveUserInformation: called");
        User userInformation = new User(userID, username, email, name, surname, number, profileImgURL, new GeoPoint(latitude, longitude));
        SingletonUser singletonUser = SingletonUser.getInstance();
        singletonUser.user = userInformation;
        final DocumentReference documentReference = FirebaseObject.fStore.collection("users").document(userID).collection("user information").document("user information");
        documentReference.set(userInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegistrationActivity.this,"Profile created.", Toast.LENGTH_SHORT).show();
                Map<String, Object> data = new HashMap<>();
                data.put("test", "test");
                FirebaseObject.fStore.collection("users").document(userID).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
            }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegistrationActivity.this, "User went wrong, Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
