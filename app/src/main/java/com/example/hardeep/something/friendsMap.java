package com.example.hardeep.something;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class friendsMap extends FragmentActivity {

    private GoogleMap mMap;
    public TextView mapFriendsName, lastSeen;
    public String sUName, sNName, city, state, country, zip, myUsername, myName;
    public Button requestUpate;
    public boolean trafficStatus;
    public int mapType;
    public double latitude, longitude;
    public CameraUpdate location;
    public ProgressBar pBar;
    public SharedPreferences pref;
    public Date time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_map);

        //get Preferences
        pref = PreferenceManager.getDefaultSharedPreferences(friendsMap.this);
        trafficStatus = pref.getBoolean("traffic", false);
        mapType = pref.getInt("map_type", GoogleMap.MAP_TYPE_NORMAL);
        myUsername = pref.getString("Username", null);
        myName = pref.getString("Name", null);

        //get selected ID
        Intent i = getIntent();
        sUName = i.getStringExtra("fUsername");
        sNName = i.getStringExtra("fName");

        //Intialize variables and hide until map is loaded
        pBar = (ProgressBar) findViewById(R.id.fMapPBar);
        lastSeen = (TextView) findViewById(R.id.lastSeen);
        mapFriendsName = (TextView) findViewById(R.id.mapFriendsID);
        requestUpate = (Button) findViewById(R.id.requestUpdate);
        mapFriendsName.setText(sUName + ": " + sNName);
        mapFriendsName.setVisibility(View.GONE);
        requestUpate.setVisibility(View.GONE);
        lastSeen.setVisibility(View.GONE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("Username", sUName);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    if (object.getNumber("Latitude") != null) {
                        latitude = object.getNumber("Latitude").doubleValue();
                        longitude = object.getNumber("Longitude").doubleValue();
                        city = object.getString("City");
                        state = object.getString("State");
                        country = object.getString("Country");
                        zip = object.getString("Zip");
                        time = object.getUpdatedAt();

                        //setup map
                        setUpMapIfNeeded();
                        lastSeen.setText((time.getMonth() + 1) + "/" + time.getDate() + "/20" + (time.getYear() - 100) +
                                " -- " + time.getHours() + ":" + time.getMinutes());
                    } else {
                        pBar.setVisibility(View.GONE);
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(friendsMap.this)
                                .setTitle("Request Update")
                                .setMessage("It appears " + sUName + ": " + sNName + " never updated their location. Would you like to request update?")
                                .setPositiveButton("Request", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendNotification();
                                    }
                                })
                                .setNegativeButton("NeverMind", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Intent i = new Intent(friendsMap.this, Friendss.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                        dialog.create().show();
                    }
                } else {
                    Toast.makeText(friendsMap.this, "Server Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        requestUpate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send notification to selected friend
                sendNotification();
            }
        });


    }

    public void sendNotification() {
        ParseQuery sQuery = ParseInstallation.getQuery();
        sQuery.whereEqualTo("Username", sUName);
        ParsePush push = new ParsePush();
        push.setQuery(sQuery);
        JSONObject data = new JSONObject();
        try {
            data.put("username", myUsername);
            data.put("name", myName);
            data.put("action", "updateRequest");
        }catch (Exception e) {}
        push.setData(data);
        push.sendInBackground();
        Toast.makeText(friendsMap.this, "Request Sent!", Toast.LENGTH_SHORT).show();
    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
*/
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.friendsMap))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //Get map
        mMap.setMyLocationEnabled(false);
        mMap.setTrafficEnabled(trafficStatus);
        mMap.setMapType(mapType);
        LatLng latLng = new LatLng(latitude, longitude);
        location = CameraUpdateFactory.newLatLngZoom(latLng, 12);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(location);
                mapFriendsName.setVisibility(View.VISIBLE);
                requestUpate.setVisibility(View.VISIBLE);
                lastSeen.setVisibility(View.VISIBLE);
                pBar.setVisibility(View.GONE);
            }
        });
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(sUName + ": " + sNName + " is here")
                .snippet(city + ", " + state + " " + country + " " + zip));

    }
}
