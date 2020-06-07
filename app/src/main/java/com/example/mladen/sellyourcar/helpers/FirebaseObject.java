package com.example.mladen.sellyourcar.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseObject {

    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseFirestore fStore;
    public static FirebaseStorage storage;
    public static StorageReference storageReference;

    public static void InitalizeFirebase(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }
}
