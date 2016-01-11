package com.example.hardeep.something;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;


public class Information extends Activity {

    public EditText id;
    public EditText name;
    public Button next;
    public SharedPreferences idPref;
    public String username, sName;
    public ParseObject server;
    public TelephonyManager telephonyManager;
    public String deviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = telephonyManager.getDeviceId();

        server = new ParseObject("Location");
        id = (EditText) findViewById(R.id.settingsId);
        name = (EditText) findViewById(R.id.settingsName);
        next = (Button) findViewById(R.id.settingUpdate);

        idPref = PreferenceManager.getDefaultSharedPreferences(Information.this);
        idPref.edit().putBoolean("first_time", true).apply();

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = id.getText().toString().trim();
                sName = name.getText().toString().trim();
                if (username.equals("") || sName.equals("")) {
                    Toast.makeText(Information.this, "Please provide all the Information", Toast.LENGTH_SHORT).show();
                } else {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                    query.whereEqualTo("Username", username);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (object == null) {
                                idPref.edit().putString("Username", username).apply();
                                idPref.edit().putString("Name", sName).apply();
                                Intent i = new Intent(Information.this, MainMenu.class);
                                startActivity(i);
                                idPref.edit().putBoolean("first_time", false).apply();
                                Toast.makeText(Information.this, "Username Successfully Created!", Toast.LENGTH_SHORT).show();
                                server.put("Username", username);
                                server.put("Name", sName);
                                server.saveInBackground();

                                //Notify the username
                                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                installation.put("deviceID", deviceID);
                                installation.put("Username", username);
                                installation.put("Name", sName);
                                installation.saveInBackground();

                                finish();
                            } else {
                                Toast.makeText(Information.this, "Username Already Exist!", Toast.LENGTH_SHORT).show();
                                id.setText("");
                            }
                        }
                    });
                }
            }

        });
    }

}
