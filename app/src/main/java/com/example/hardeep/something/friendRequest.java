package com.example.hardeep.something;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class friendRequest extends Activity {

    public ListView requestListView;
    public String username, name, uN1, nN1;
    public ProgressBar pBar;
    public AutoCompleteTextView rActv;
    public SharedPreferences idPref;
    public rAdapter adapter;
    public List<String> serverRequests = new ArrayList<String>();
    public ArrayList<String> requests = new ArrayList<String>();
    public ArrayList<UserInfo> rData = new ArrayList<UserInfo>();

    //To only push once when activity ends
    public List<String> localAccepted = new ArrayList<String>();
    //Array to add just usernames
    public List<String> localAccept = new ArrayList<String>();
    public List<String> localDecline = new ArrayList<String>();
    public static final int TIME = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        rActv = (AutoCompleteTextView) findViewById(R.id.requestEditText);
        rActv.setDropDownHeight(0);
        pBar = (ProgressBar) findViewById(R.id.requestPBar);
        requestListView = (ListView) findViewById(R.id.requestListView);
        idPref = PreferenceManager.getDefaultSharedPreferences(friendRequest.this);
        username = idPref.getString("Username", null);
        name = idPref.getString("Name", null);

        //hide keyboard on Start up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //get Username request list
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("Username", username);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    serverRequests = object.getList("Requests");
                    if (serverRequests != null) {
                        requests.addAll(serverRequests);

                        //add to the class to store data more efficiently
                        for (int i = 0; i < requests.size(); i++) {
                            String[] token = requests.get(i).split("/");
                            rData.add(new UserInfo(token[0], token[1]));
                        }
                    } else {
                        pBar.setVisibility(View.GONE);
                        Toast.makeText(friendRequest.this, "You have no requests pending", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(friendRequest.this, "Data Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter = new rAdapter(friendRequest.this, rData);
                requestListView.setAdapter(adapter);
                rActv.setAdapter(adapter);
                pBar.setVisibility(View.GONE);
            }
        }, TIME);

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final UserInfo user = (UserInfo) parent.getItemAtPosition(position);
                TextView uN = (TextView) view.findViewById(R.id.requestName);
                TextView nN = (TextView) view.findViewById(R.id.requestTheName);
                uN1 = uN.getText().toString().trim();
                nN1 = nN.getText().toString().trim();
                AlertDialog.Builder dialog = new AlertDialog.Builder(friendRequest.this);
                dialog.setTitle("Request");
                dialog.setMessage("What would you like to do with " + uN1 + ": " + nN1 + " friend request?");
                dialog.setPositiveButton("Accept",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //add just usernames
                                localAccept.add(uN1);
                                //add username and name
                                localAccepted.add(uN1 + "/" + nN1);
                                localDecline.add(uN1 + "/" + nN1);
                                rData.remove(user);
                                adapter.notifyDataSetChanged();

                                //send confirmation
                                ParseQuery sQuery = ParseInstallation.getQuery();
                                sQuery.whereEqualTo("Username", uN1);
                                ParsePush push = new ParsePush();
                                push.setQuery(sQuery);
                                JSONObject data = new JSONObject();
                                try {
                                    data.put("username", username);
                                    data.put("name", name);
                                    data.put("action", "confirmation");
                                }catch (Exception e) {}
                                push.setData(data);
                                push.sendInBackground();
                            }
                        });
                dialog.setNegativeButton("Decline",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                localDecline.add(uN1 + "/" + nN1);
                                rData.remove(user);
                                adapter.notifyDataSetChanged();
                            }
                        });
                dialog.create().show();
            }
        });
    }


    //send to the server when back is pressed

    @Override
    protected void onStop() {
        super.onStop();
        if(serverRequests != null && uN1 != null) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
            query.whereEqualTo("Username", username);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    object.addAllUnique("Friends", localAccepted);
                    object.removeAll("Requests", localDecline);
                    object.saveInBackground();
                }
            });

            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Location");
            query2.whereContainedIn("Username", localAccept);
            query2.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null) {
                        for (ParseObject object : list) {
                            object.addAllUnique("Friends", Arrays.asList(username+"/"+name));
                            object.saveInBackground();
                        }
                    } else {
                        Toast.makeText(friendRequest.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}
