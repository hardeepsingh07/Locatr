package com.example.hardeep.something;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Handler;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Search extends Activity {

    public ListView searchListView;
    private AutoCompleteTextView actv;
    public ProgressBar pBar;
    public ArrayList<String> uNames = new ArrayList<>();
    public ArrayList<String> nNames = new ArrayList<>();
    public ArrayList<UserInfo> sData = new ArrayList<>();
    public ArrayList<String> friends = new ArrayList<>();
    public SharedPreferences idPrefs;
    public String username;
    public String name;
    public static final int TIME = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        pBar = (ProgressBar) findViewById(R.id.searchPBar);
        idPrefs = PreferenceManager.getDefaultSharedPreferences(Search.this);
        username = idPrefs.getString("Username", null);
        name = idPrefs.getString("Name", null);
        actv = (AutoCompleteTextView) findViewById(R.id.searchEditText);
        actv.setDropDownHeight(0);
        searchListView = (ListView) findViewById(R.id.searchListView);

        //hide keyboard on Start up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //get all the username and names from server
        retrieveData();

        //wait for the data from server
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(uNames != null) {
                    MyAdapter adapter = new MyAdapter(Search.this, username, name, sData, friends);
                    searchListView.setAdapter(adapter);
                    actv.setAdapter(adapter);
                    pBar.setVisibility(View.GONE);
                } else {
                    retrieveData();
                    run();
                }
            }
        }, TIME);

    }

    public void retrieveData() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
        query.whereNotEqualTo("Username", username);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    //loop through all the objects and to find username
                    for (ParseObject object : list) {
                        uNames.add(object.getString("Username"));
                        nNames.add(object.getString("Name"));
                    }
                    //add to the class to store info efficiently
                    for (int s = 0; s < uNames.size(); s++) {
                        sData.add(new UserInfo(uNames.get(s), nNames.get(s)));
                    }
                } else {
                    Toast.makeText(Search.this, "Data Error Occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //get users friends list
        ParseQuery<ParseObject> q = ParseQuery.getQuery("Location");
        q.whereEqualTo("Username", username);
        q.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                List<String> f = object.getList("Friends");
                if (f != null) {
                    friends.addAll(f);
                }
            }
        });
    }
}
