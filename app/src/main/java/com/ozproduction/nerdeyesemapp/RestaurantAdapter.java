package com.ozproduction.nerdeyesemapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
public class RestaurantAdapter extends ArrayAdapter<Restaurant> implements View.OnClickListener{
    private Restaurant[] dataSet;
    Context mContext;
    private static class ViewHolder {
        ImageView restauranticon;
        TextView restaurantname;
        Button view;
    }
    public RestaurantAdapter(Restaurant[] data, Context context) {
        super(context, R.layout.row_item, data);
        this.dataSet = data;
        this.mContext=context;
    }
    @Override
    public void onClick(View v) {
        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Restaurant restaurant =(Restaurant)object;

    }
    private int lastPosition = -1;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Restaurant restaurant = getItem(position);
        final ViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.restauranticon = (ImageView) convertView.findViewById(R.id.restauranticon);
            viewHolder.restaurantname = (TextView) convertView.findViewById(R.id.restaurantname);
            //viewHolder.view = (Button) convertView.findViewById(R.id.viewbtn);
            result=convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;
        viewHolder.restaurantname.setText(restaurant.getName());
        return convertView;
    }
}
