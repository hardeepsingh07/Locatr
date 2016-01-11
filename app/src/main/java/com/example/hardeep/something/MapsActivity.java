package com.example.hardeep.something;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    public double longitude, latitude;
    public FloatingActionsMenu menu;
    public FloatingActionButton saveSend, bServer;
    public Bitmap bitmap;
    public ParseObject server;
    public File directory, file;
    public boolean saved = false;
    public String city, state, country, zip;
    public String username, name;
    public boolean trafficStatus, Run;
    public int mapType;
    public SharedPreferences pref;
    public TextView displayUsername;
    public ProgressBar pBar;
    public CameraUpdate location;
    public final static int TIME = 1000;
    public LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Cancel the notification
        NotificationManager notification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.cancel(1);

        //Intialize LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //check if gps is on
        checkGPS();

        //Create a class on Server
        server = new ParseObject("Location");

        //get Preferences
        pref = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
        username = pref.getString("Username", null);
        name = pref.getString("Name", null);
        trafficStatus = pref.getBoolean("traffic", false);
        mapType = pref.getInt("map_type", GoogleMap.MAP_TYPE_NORMAL);

        //Initialize buttons
        saveSend = (FloatingActionButton) findViewById(R.id.fabSendButton);
        bServer = (FloatingActionButton) findViewById(R.id.fabSaveButton);
        menu = (FloatingActionsMenu) findViewById(R.id.menu);
        displayUsername = (TextView) findViewById(R.id.mapUsername);
        pBar = (ProgressBar) findViewById(R.id.mapPbar);
        displayUsername.setText(username);

        //Invisible until map is loaded
        Run = true;
        menu.setVisibility(View.GONE);
        displayUsername.setVisibility(View.GONE);

        saveSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pBar.setVisibility(View.VISIBLE);
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                            @Override
                            public void onSnapshotReady(Bitmap snapshot) {
                                bitmap = snapshot;
                                try {
                                    directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Screenshots");
                                    String filename = System.currentTimeMillis() + ".png";
                                    file = new File(directory, filename);
                                    FileOutputStream out = new FileOutputStream(file);
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                                    out.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MapsActivity.this, "File saved in 'Screenshots' folder", Toast.LENGTH_SHORT).show();
                        Uri uri = null;
                        try {
                            uri = Uri.parse(file.toURL().toString());
                        } catch (Exception e) {
                        }
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.putExtra("sms_body", "My Location...");
                        i.setType("image/png");
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(i);
                        pBar.setVisibility(View.INVISIBLE);
                    }
                }, TIME);
            }
        });

        bServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pBar.setVisibility(View.VISIBLE);
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo("Username", username);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            object.put("Longitude", longitude);
                            object.put("Latitude", latitude);
                            object.put("City", city);
                            object.put("State", state);
                            object.put("Country", country);
                            object.put("Zip", zip);
                            object.saveInBackground();
                            Toast.makeText(MapsActivity.this, "Location Updated on Server!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MapsActivity.this, "Error has occured, cannot update location on server", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                pBar.setVisibility(View.INVISIBLE);
            }
        });
    }

   /* @Override
    protected void onStart() {
        super.onStart();
        checkGPS();
    }*/


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //Get map
        mMap.setTrafficEnabled(trafficStatus);
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(mapType);
        mMap.setOnMyLocationChangeListener(update);
}
    public GoogleMap.OnMyLocationChangeListener update = new GoogleMap.OnMyLocationChangeListener() {
        LatLng latLng;
        @Override
        public void onMyLocationChange(Location locale) {
            if(Run) {
                latitude = locale.getLatitude();
                longitude = locale.getLongitude();
                latLng = new LatLng(latitude, longitude);
                location = CameraUpdateFactory.newLatLngZoom(latLng, 12);
                try {
                    getLocationName();
                } catch (Exception e) {
                }
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMap.animateCamera(location);
                        update = null;
                        mMap.addMarker(new MarkerOptions().position(latLng).title("You are here")
                                .snippet(city + ", " + state + " " + country + " " + zip));
                        Run = false;

                        displayUsername.setVisibility(View.VISIBLE);
                        menu.setVisibility(View.VISIBLE);
                        pBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    };

    //Get Specific location in Words
    private void getLocationName() throws Exception {
        Geocoder geo = new Geocoder(MapsActivity.this);
        List<Address> addressList = geo.getFromLocation(latitude, longitude, 1);
        if (addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);
            city = address.getLocality();
            state = address.getAdminArea();
            country = address.getCountryName();
            zip = address.getPostalCode();
        }
    }

    public void checkGPS() {
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(MapsActivity.this, "GPS Confirmed On!", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
            dialog.setTitle("GPS");
            dialog.setMessage("GPS is disabled in your device. Enable it?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Enable GPS",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(settingsIntent);
                        }
                    });
            dialog.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MapsActivity.this, "GPS must be turned on for a location", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            Intent i = new Intent(MapsActivity.this, MainMenu.class);
                            startActivity(i);
                            finish();
                        }
                    });
            dialog.create().show();
        }
    }
}
