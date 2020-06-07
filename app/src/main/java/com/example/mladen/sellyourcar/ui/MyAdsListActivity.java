package com.example.mladen.sellyourcar.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.models.Ad;
import com.example.mladen.sellyourcar.models.MyAds;

import java.util.ArrayList;

public class MyAdsListActivity extends AppCompatActivity {

    private ArrayList<String> myAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ads_list);

        myAds = new ArrayList<>();

        ListView myAdsListView = (ListView) findViewById(R.id.my_ads_list);

        myAdsListView.setAdapter(new ArrayAdapter<Ad>(this, android.R.layout.simple_list_item_1, MyAds.GetInstance().GetMyAds()));
    }
}
