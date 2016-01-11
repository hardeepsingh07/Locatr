package com.example.hardeep.something;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;

import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class Welcome extends Activity {

    public static final int TIME = 2000;
    public TelephonyManager telephonyManager;
    public String deviceID, username, name;
    public SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //Get Phone ID
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = telephonyManager.getDeviceId();

        //Get Preferences to set values later
        prefs = PreferenceManager.getDefaultSharedPreferences(Welcome.this);

        //check if already have an account
        checkRepition();

        //handle moving on
        moveOn();
    }


    public void checkRepition() {
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("deviceID", deviceID);
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (parseObject != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
                    builder.setTitle("Account Found");
                    builder.setMessage("It looks this device already have an account, Would you like to do?");
                    builder.setPositiveButton("Create New", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                parseObject.delete();
                                parseObject.saveInBackground();
                                moveOn();
                            } catch (Exception e) {
                                Toast.makeText(Welcome.this, "Something went wrong, Please try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    builder.setNegativeButton("Retrieve Info", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            username = parseObject.getString("Username");
                            name = parseObject.getString("Name");

                            prefs.edit().putString("Username", username).apply();
                            prefs.edit().putString("Name", name).apply();
                        }
                    }).show();
                } else {
                    moveOn();
                }
            }
        });
    }

    public void moveOn()    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences time = PreferenceManager.getDefaultSharedPreferences(Welcome.this);
                if (time.getBoolean("first_time", true)) {
                    Intent i = new Intent(Welcome.this, Information.class);
                    startActivity(i);
                    time.edit().putBoolean("first_time", false).commit();
                    finish();
                } else {
                    Intent i = new Intent(Welcome.this, MainMenu.class);
                    startActivity(i);
                    finish();
                }
            }
        }, TIME);
    }
}
