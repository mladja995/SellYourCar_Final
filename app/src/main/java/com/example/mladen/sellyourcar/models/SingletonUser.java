package com.example.mladen.sellyourcar.models;

import java.util.ArrayList;

public class SingletonUser {

    private static SingletonUser singleton_instance = null;

    public User user;
    public ArrayList<Ad> myAds;
    private SingletonUser()
    {
        user = new User();
        myAds = new ArrayList<Ad>();
    }

    public static SingletonUser getInstance()
    {
        if (singleton_instance == null)
            singleton_instance = new SingletonUser();

        return singleton_instance;
    }
}
