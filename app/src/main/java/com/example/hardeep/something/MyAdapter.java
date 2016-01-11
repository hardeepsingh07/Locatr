package com.example.hardeep.something;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.PushService;

import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyAdapter extends ArrayAdapter<UserInfo>{

    public Context context;
    public String selection;
    public String username;
    public String name;
    public ArrayList<String> friends = new ArrayList<>();
    public List<UserInfo> mUsers;

    public MyAdapter(Context context, String username, String name, ArrayList<UserInfo> sData, ArrayList<String> friends) {
        super(context, R.layout.search_layout, sData);
        this.context = context;
        this.username = username;
        this.name = name;
        this.friends = friends;

        //clone to filter through the list
        mUsers = new ArrayList<UserInfo>(sData.size());
        mUsers.addAll(sData);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserInfo user = getItem(position);

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.search_layout, parent, false);
        }

        final TextView uName = (TextView) convertView.findViewById(R.id.searchText);
        final TextView nName = (TextView) convertView.findViewById(R.id.searchNameText);
        final Button addFriends = (Button) convertView.findViewById(R.id.addFriend);

        //check if user already a friend
        for(String s: friends) {
            String [] token = s.split("/");
            if(token[0].equals(user.getUsername())) {
                addFriends.setEnabled(false);
                addFriends.setText("Friends");
                addFriends.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        uName.setText(user.getUsername());
        nName.setText(user.getName());

        addFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = uName.getText().toString();
                //add current user to selected friends, request list
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Location");
                query.whereEqualTo("Username", selection);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (object != null) {
                            object.addAllUnique("Requests", Arrays.asList(username + "/" + name));
                            object.saveInBackground();
                            Toast.makeText(context, "Friend request is sent to " + selection, Toast.LENGTH_SHORT).show();
                            addFriends.setEnabled(false);
                            addFriends.setBackgroundColor(Color.TRANSPARENT);
                            addFriends.setText("Requested");
                        } else {
                            Toast.makeText(context, "Error has occured on server", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //send notification to selected friend
                ParseQuery sQuery = ParseInstallation.getQuery();
                sQuery.whereEqualTo("Username", selection);
                ParsePush push = new ParsePush();
                push.setQuery(sQuery);
                JSONObject data = new JSONObject();
                try {
                    data.put("username", username);
                    data.put("name", name);
                    data.put("action", "friendRequest");
                }catch (Exception e) {}
                push.setData(data);
                push.sendInBackground();
            }
        });
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if(constraint != null) {
                ArrayList<UserInfo> suggestions =  new ArrayList<UserInfo>();
                for(UserInfo user: mUsers) {
                    if(user.getUsername().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(user);
                    }
                }
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
            }
            return  filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
           clear();
            if(results != null && results.count > 0) {
                addAll((ArrayList<UserInfo>) results.values);
            } else {
                addAll(mUsers);
            }
            notifyDataSetChanged();
        }
    };
}