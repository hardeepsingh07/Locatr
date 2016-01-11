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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Friendss extends Activity {

    public ListView friendsListView;
    public List<String> names = new ArrayList<String>();
    public SharedPreferences idPref;
    public String username;
    public ProgressBar pBar;
    public AutoCompleteTextView fActv;
    public ArrayList<String> friendNames = new ArrayList<String>();
    public ArrayList<UserInfo> fData = new ArrayList<UserInfo>();
    public final static int TIME = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friendss);

        fActv = (AutoCompleteTextView) findViewById(R.id.friendsEditText);
        fActv.setDropDownHeight(0);
        pBar = (ProgressBar) findViewById(R.id.friendsPBar);
        friendsListView = (ListView) findViewById(R.id.friendsListView);
        idPref = PreferenceManager.getDefaultSharedPreferences(Friendss.this);
        username = idPref.getString("Username", null);

        //hide keyboard on Start up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);



        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereEqualTo("Username", username);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    names = object.getList("Friends");
                    if(names != null) {
                        friendNames.addAll(names);

                        //add to the class to store date more efficiently
                        for(int i = 0; i < friendNames.size(); i++) {
                            String[] token = friendNames.get(i).split("/");
                            fData.add(new UserInfo(token[0], token[1]));
                        }
                    } else {
                        pBar.setVisibility(View.GONE);
                        Toast.makeText(Friendss.this, "You currently have no friends", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Friendss.this, "Data Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //wait for server to fetch to adapter
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fAdapter adapter = new fAdapter(Friendss.this, fData);
                friendsListView.setAdapter(adapter);
                fActv.setAdapter(adapter);
                pBar.setVisibility(View.GONE);
            }
        }, TIME);

       friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
               TextView uN = (TextView) view.findViewById(R.id.fName);
               TextView nN = (TextView) view.findViewById(R.id.fTheName);
               String uN1 = uN.getText().toString().trim();
               String nN1 = nN.getText().toString().trim();
               Intent i = new Intent(Friendss.this, friendsMap.class);
               i.putExtra("fUsername", uN1);
               i.putExtra("fName", nN1);
               startActivity(i);
           }
       });
    }
}
