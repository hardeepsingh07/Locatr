package com.example.hardeep.something;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

public class MyReceiver extends ParsePushBroadcastReceiver{

    public Context context;
    public String action = "";
    public String incomingUsername;
    public String incomingName;
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        this.context = context;
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            incomingUsername = json.getString("username");
            incomingName = json.getString("name");
            action = json.getString("action");
        } catch (Exception e) {}

        switch (action) {
            case "friendRequest":
                //send to the request page
                PendingIntent i = PendingIntent.getActivity(context, 0, new Intent(context, friendRequest.class), 0);

                //prepare notification
                Notification notification = new NotificationCompat.Builder(context)
                        .setContentTitle("Friend Request")
                        .setSmallIcon(R.drawable.location)
                        .setContentText(incomingUsername + ": " + incomingName +
                                " is requesting to be your friend")
                        .setContentIntent(i)
                        .setAutoCancel(true)
                        .build();
                notification.defaults |= Notification.DEFAULT_LIGHTS;
                notification.defaults |= Notification.DEFAULT_VIBRATE;
                notification.defaults |= Notification.DEFAULT_SOUND;

                //get the System notification manager
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(0, notification);
                break;
            case "updateRequest":
                //Go to Map Activity for Button
                PendingIntent maps = PendingIntent.getActivity(context, 0,
                        new Intent(context, MapsActivity.class), 0);
                //Go to main paage on notification click
                PendingIntent cI = PendingIntent.getActivity(context, 0,
                        new Intent(context, MainMenu.class), 0);

                //Prepare the notification with custom view
                Notification notifications = new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setContentTitle("Update Request")
                        .setContentText(incomingUsername + ": " + incomingName +
                        " is requesting for your updated location")
                        .setSmallIcon(R.drawable.location)
                        .addAction(R.drawable.update, "Update", maps)
                        .addAction(R.drawable.open32, "Open", cI).build();
                notifications.defaults |= Notification.DEFAULT_LIGHTS;
                notifications.defaults |= Notification.DEFAULT_VIBRATE;
                notifications.defaults |= Notification.DEFAULT_SOUND;

                //get the system notification service manager
                NotificationManager manager = (NotificationManager)
                        context.getSystemService(context.NOTIFICATION_SERVICE);

                manager.notify(1,notifications);
                break;
            case "confirmation":
                //send to the request page
                PendingIntent j = PendingIntent.getActivity(context, 0, new Intent(context, MainMenu.class), 0);

                //prepare notification
                Notification notif = new NotificationCompat.Builder(context)
                        .setContentTitle("Friend Request")
                        .setSmallIcon(R.drawable.location)
                        .setContentText(incomingUsername + ": " + incomingName +
                                " have accepted your request.")
                        .setContentIntent(j)
                        .setAutoCancel(true)
                        .build();
                notif.defaults |= Notification.DEFAULT_LIGHTS;
                notif.defaults |= Notification.DEFAULT_VIBRATE;
                notif.defaults |= Notification.DEFAULT_SOUND;

                //get the System notification manager
                NotificationManager notifManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                notifManager.notify(3, notif);
                break;
        }
    }
}
