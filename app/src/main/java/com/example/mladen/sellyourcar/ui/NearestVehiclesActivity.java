package com.example.mladen.sellyourcar.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.mladen.sellyourcar.adapters.CustomAdapter;
import com.example.mladen.sellyourcar.helpers.FirebaseObject;
import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.models.SingletonUser;
import com.example.mladen.sellyourcar.models.User;

import java.util.ArrayList;

public class NearestVehiclesActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NearestVehiclesActivity";
    private RecyclerView recyclerView;
    private CustomAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    //Test
    String[] myDataset = { "prvi element", "drugi element", "treci element"};

    Button uploadImageButton, saveInCurrentLocationButton, saveInAnotherLoacationButton, searchButton, postButton, saveButton, myAdsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_vehicles);
        FirebaseObject.InitalizeFirebase();
        InitializeRecyclerView();
        InitializeButtons();

        Log.d(TAG, "onCreate: User email: " + SingletonUser.getInstance().user.email);


    }



    private void InitializeRecyclerView()
    {
        recyclerView = (RecyclerView) findViewById(R.id.activity_nearest_vehicles_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)

        //Test
        ArrayList<CustomAdapter.AdItem> adItems = new ArrayList<>();
        for (int i=0; i<myDataset.length; i++)
        {
            adItems.add(new CustomAdapter.AdItem(myDataset[i]));
        }

        mAdapter = new CustomAdapter(adItems);
        recyclerView.setAdapter(mAdapter);
    }

    private void InitializeButtons()
    {
        searchButton = (Button) findViewById(R.id.post_an_ad_edit_search_button);
        postButton = (Button) findViewById(R.id.post_an_ad_edit_post_button);
        saveButton = (Button) findViewById(R.id.post_an_ad_edit_save_button);
        myAdsButton = (Button) findViewById(R.id.post_an_ad_edit_my_ads_button);

        searchButton.setOnClickListener(this);
        postButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        myAdsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        /*switch (view.getId())
        {
            case R.id.post_an_ad_edit_upload_button:
            {
                break;
            }
            case R.id.post_an_ad_edit_my_ads_button:
            {
                Intent intent = new Intent(this, PostAnAdActivity.class);
                startActivity(intent);
                break;
            }

        }*/

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
            Intent i = new Intent(NearestVehiclesActivity.this, MapsActivity.class);
            startActivity(i);
        }

        else if(id == R.id.profile_item)
        {
            /*Toast.makeText(this, "profile_item!", Toast.LENGTH_SHORT).show();*/
            Intent intent = new Intent(NearestVehiclesActivity.this, PostAnAdActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.message_item)
        {
            Toast.makeText(this, "message_item!", Toast.LENGTH_SHORT).show();
        }
        else if(id == R.id.logout_item)
        {
            FirebaseObject.mFirebaseAuth.getInstance().signOut();
            Intent i = new Intent(NearestVehiclesActivity.this, LoginActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }




}
