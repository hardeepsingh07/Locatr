package com.example.hardeep.something;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class Settings extends Activity {

    public Button update;
    public EditText name, id;
    public SharedPreferences idPref;
    public RadioGroup mapType, traffic;
    public RadioButton rMapType, rTraffic;
    public String username, sName, mapText, trafficText;
    public int mapSelectionId, trafficSelectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Intialize values
        update = (Button) findViewById(R.id.settingUpdate);
        name = (EditText) findViewById(R.id.settingsName);
        id = (EditText) findViewById(R.id.settingsId);
        mapType = (RadioGroup) findViewById(R.id.mapGroup);
        traffic = (RadioGroup) findViewById(R.id.trafficGroup);

        //get value from preferences
        idPref = PreferenceManager.getDefaultSharedPreferences(Settings.this);
        username = idPref.getString("Username", "Error");
        sName = idPref.getString("Name", "Error");
        mapSelectionId = idPref.getInt("radio_map", R.id.radioNormal);
        trafficSelectionId = idPref.getInt("radio_traffic", R.id.trafficOff);
        rMapType  = (RadioButton) findViewById(mapSelectionId);
        rTraffic = (RadioButton) findViewById(trafficSelectionId);

        rMapType.setChecked(true);
        rTraffic.setChecked(true);

        //set the values
        id.setText(username);
        name.setText(sName);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapSelectionId = mapType.getCheckedRadioButtonId();
                trafficSelectionId = traffic.getCheckedRadioButtonId();

                rMapType = (RadioButton) findViewById(mapSelectionId);
                rTraffic = (RadioButton) findViewById(trafficSelectionId);

                mapText = rMapType.getText().toString();
                trafficText = rTraffic.getText().toString();

                checkSelections();

                //To update view upon return
                idPref.edit().putInt("radio_map", mapSelectionId).apply();
                idPref.edit().putInt("radio_traffic", trafficSelectionId).apply();
                idPref.edit().putString("Name", name.getText().toString().trim()).apply();

                Intent i = new Intent(Settings.this, MainMenu.class);
                startActivity(i);

                Toast.makeText(Settings.this, "Updated", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void checkSelections()    {
        switch (mapText) {
            case "Generic":
                idPref.edit().putInt("map_type", GoogleMap.MAP_TYPE_NORMAL).apply();
                break;
            case "Satellite":
                idPref.edit().putInt("map_type", GoogleMap.MAP_TYPE_SATELLITE).apply();
                break;
            case "Terrain":
                idPref.edit().putInt("map_type", GoogleMap.MAP_TYPE_TERRAIN).apply();
                break;
        }

        switch (trafficText) {
            case "On ":
                idPref.edit().putBoolean("traffic", true).apply();
                break;
            case "Off":
                idPref.edit().putBoolean("traffic", false).apply();
                break;
        }
    }
}
