package com.example.hardeep.something;


import android.app.Application;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

public class ParseApplicatoin extends Application {

    public String serverId = "7THUJ53rCmTp8uJjIrZJ97fZ14JMUfjnTbr65eiO";
    public String clientId = "K3Np4HTPJNBdStrVEVyTiAnIilyPchi5gDY3uEwF";
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(ParseApplicatoin.this);
        Parse.initialize(this, serverId, clientId);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
