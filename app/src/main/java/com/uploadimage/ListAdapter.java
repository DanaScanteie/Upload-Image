package com.uploadimage;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<URLDetails> {

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    private static class ViewHolder {
        TextView name;
        TextView url;
    }

    public ListAdapter(Context context, int resource, ArrayList<URLDetails> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position).getName();
        String url = getItem(position).getUrl();

        URLDetails urlDetails = new URLDetails(name,url);

        final View result;
        ViewHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder= new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.textView1);
            holder.url = (TextView) convertView.findViewById(R.id.textView2);

            result = convertView;

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }


        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        result.startAnimation(animation);
        lastPosition = position;

        holder.name.setText(urlDetails.getName());
        holder.url.setText(urlDetails.getUrl());

        return convertView;
    }
}
