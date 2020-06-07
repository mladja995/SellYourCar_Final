package com.example.mladen.sellyourcar.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User implements Parcelable {

    public String userID;
    public String username;
    public String email;
    public String name;
    public String surname;
    public String number;
    public String profileImgURL;
    public GeoPoint cordinates;
    @Exclude
    public ArrayList<Ad> myAds;


   public User (){ }

   public User(String userID, String username, String email, String name, String surname,
               String number, String profileImgURL, GeoPoint cordinates)
   {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.number = number;
        this.profileImgURL = profileImgURL;
        this.cordinates = cordinates;
        this.myAds = new ArrayList<Ad>();

    }


    /*Making User class parcelable*/
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(userID);
        out.writeString(username);
        out.writeString(email);
        out.writeString(name);
        out.writeString(surname);
        out.writeString(number);
        out.writeString(profileImgURL);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in)
        {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }};

    private User(Parcel in) {
        userID = in.readString();
        username = in.readString();
        email = in.readString();
        name = in.readString();
        surname = in.readString();
        number = in.readString();
        profileImgURL = in.readString();
    }




}
