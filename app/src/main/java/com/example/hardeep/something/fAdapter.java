package com.example.hardeep.something;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class fAdapter extends ArrayAdapter<UserInfo> {

    public Context context;
    public ArrayList<UserInfo> mUsers;
    public fAdapter(Context context, ArrayList<UserInfo> fData) {
        super(context, R.layout.listview_layout, fData);
        this.context = context;

        //clone to filter through the list
        mUsers = new ArrayList<UserInfo>(fData.size());
        mUsers.addAll(fData);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserInfo user = getItem(position);

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview_layout, parent, false);
        }

        TextView fUsername = (TextView) convertView.findViewById(R.id.fName);
        TextView fName = (TextView) convertView.findViewById(R.id.fTheName);

        fUsername.setText(user.getUsername());
        fName.setText(user.getName());

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
