package com.example.hardeep.something;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.parse.ParseInstallation;
import com.parse.ParseUser;


public class MainMenu extends Activity {

    ImageView mas, location, settings, request, friends, search;
    SubActionButton loc, set, req, fri, sea;
    SubActionButton.Builder itemBuilder;
    FloatingActionButton startButton;
    FloatingActionMenu menu;
    Animation rotate, rotate1;
    String selection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

       /* TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String maybe = telephonyManager.getDeviceId();*/

        //Cancel the notification
        NotificationManager notification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.cancel(1);

        //lock the view to Potrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //Declare ImageViews
        mas = new ImageView(MainMenu.this);
        location = new ImageView(MainMenu.this);
        settings = new ImageView(MainMenu.this);
        request = new ImageView(MainMenu.this);
        friends = new ImageView(MainMenu.this);
        search = new ImageView(MainMenu.this);

        //animation Transition
        rotate = AnimationUtils.loadAnimation(MainMenu.this, R.anim.rotate);
        rotate1 = AnimationUtils.loadAnimation(MainMenu.this, R.anim.rotate1);

        //Intialize ImageViews
        mas.setImageDrawable(getResources().getDrawable(R.drawable.location));
        location.setImageDrawable(getResources().getDrawable(R.drawable.navigation));
        settings.setImageDrawable(getResources().getDrawable(R.drawable.settings));
        request.setImageDrawable(getResources().getDrawable(R.drawable.requests));
        friends.setImageDrawable(getResources().getDrawable(R.drawable.friends));
        search.setImageDrawable(getResources().getDrawable(R.drawable.search));

        //Put ImageViews in the builder
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(350, 350);
        itemBuilder = new SubActionButton.Builder(MainMenu.this);
        loc = itemBuilder.setContentView(location).setLayoutParams(params)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.background)).build();
        set = itemBuilder.setContentView(settings).setLayoutParams(params)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.background)).build();
        req = itemBuilder.setContentView(request).setLayoutParams(params)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.background)).build();
        fri = itemBuilder.setContentView(friends).setLayoutParams(params)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.background)).build();
        sea = itemBuilder.setContentView(search).setLayoutParams(params)
                .setBackgroundDrawable(getResources().getDrawable(R.drawable.background)).build();

        //Make the start button
        startButton = new FloatingActionButton.Builder(MainMenu.this)
                    .setBackgroundDrawable(getResources().getDrawable(R.drawable.background))
                    .setContentView(mas).setPosition(FloatingActionButton.POSITION_LEFT_CENTER)
                .setLayoutParams(new FloatingActionButton.LayoutParams(400, 400)).build();
        startButton.setAnimation(rotate);

        RelativeLayout view = (RelativeLayout) findViewById(R.id.mainMenuLayout);

        //Add the item in the menu
        menu = new FloatingActionMenu.Builder(MainMenu.this)
                .setRadius(1000)
                .setStartAngle(-45)
                .setEndAngle(45)
                .addSubActionView(req).addSubActionView(fri).addSubActionView(loc)
                .addSubActionView(sea).addSubActionView(set).attachTo(startButton).build();



        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = "location";
                v.startAnimation(rotate1);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = "search";
                v.startAnimation(rotate1);
            }
        });

        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = "friends";
                v.startAnimation(rotate1);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = "settings";
                v.startAnimation(rotate1);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = "request";
                v.startAnimation(rotate1);
            }
        });

        //Control the Animations
        rotate1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent i;
                switch (selection) {
                    case "location":
                        i = new Intent(MainMenu.this, MapsActivity.class);
                        startActivity(i);
                        break;
                    case "search":
                        i = new Intent(MainMenu.this, Search.class);
                        startActivity(i);
                        break;
                    case "friends":
                        i = new Intent(MainMenu.this, Friendss.class);
                        startActivity(i);
                        break;
                    case "settings":
                        i = new Intent(MainMenu.this, Settings.class);
                        startActivity(i);
                        break;
                    case "request":
                        i = new Intent(MainMenu.this, friendRequest.class);
                        startActivity(i);
                        break;
                }
                selection = "";
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }
}
