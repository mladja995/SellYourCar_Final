package com.example.mladen.sellyourcar.models;

import android.app.Application;

import java.util.ArrayList;


public class MarkersClient extends Application {

    private ArrayList<ClusterMarker> markers = null;

    public ArrayList<ClusterMarker> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<ClusterMarker> markers) {
        this.markers = markers;
    }
}
