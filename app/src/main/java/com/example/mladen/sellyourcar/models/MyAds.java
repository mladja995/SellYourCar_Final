package com.example.mladen.sellyourcar.models;

import java.util.ArrayList;
import java.util.List;

public class MyAds {

    private ArrayList<Ad> myAds;

    public MyAds()
    {
        myAds = new ArrayList<Ad>();
    }

    private static class SingletonHolder
    {
        public static final MyAds instance = new MyAds();
    }

    public static MyAds GetInstance()
    {
        return SingletonHolder.instance;
    }

    public ArrayList<Ad> GetMyAds()
    {
        return this.myAds;
    }

    public void AddNewAd(Ad newAd)
    {
        myAds.add(newAd);
    }

    public void DeleteAd(int index)
    {
        myAds.remove(index);
    }

    public void UpdateAd(int index, String imageUrl, String description, String mark, String model, String year, String mileage, String fuel )
    {
        Ad ad = myAds.get(index);
        ad.imageUrl = imageUrl;
        ad.description = description;
        ad.mark = mark;
        ad.model = model;
        ad.year = year;
        ad.mileage = mileage;
        ad.fuel = fuel;
    }
}
