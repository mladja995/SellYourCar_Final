package com.example.mladen.sellyourcar.models;

import com.google.firebase.firestore.GeoPoint;

public class Ad {

    public String imageUrl;
    public String description;
    public String mark;
    public String model;
    public String year;
    public String mileage;
    public String fuel;
    public GeoPoint coordinates;

    public Ad(){}

    public Ad(String imageUrl, String description, String mark, String model, String year,
              String mileage, String fuel, GeoPoint coordinates)
    {
        this.imageUrl = imageUrl;
        this.description = description;
        this.mark = mark;
        this.model = model;
        this.year = year;
        this.mileage = mileage;
        this.fuel = fuel;
        this.coordinates = coordinates;
    }



}
