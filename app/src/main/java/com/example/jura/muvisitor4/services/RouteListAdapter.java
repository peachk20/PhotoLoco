package com.example.jura.muvisitor4.services;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jura.muvisitor4.MainActivity;
import com.example.jura.muvisitor4.R;
import com.example.jura.muvisitor4.objects.RouteItem;

import java.util.ArrayList;

/**
 * Created by Jura on 5/6/2015.
 */
public class RouteListAdapter extends BaseAdapter {
    private MUVisitorDBAdapter dbAdapter;
    public ArrayList<RouteItem> routeItemList = new ArrayList<>();
    private Context context;
    private int layoutResourceId;

    public RouteListAdapter(Context context, int layoutResourceId, MUVisitorDBAdapter dbAdapter) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.dbAdapter = dbAdapter;
        this.populateRouteNames();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return routeItemList.size();
    }

    @Override
    public RouteItem getItem(int arg0) {
        // TODO Auto-generated method stub
        return routeItemList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int arg0, View convertView, ViewGroup parent) {
        final ViewHolder mHolder;
        RouteItem rItem = routeItemList.get(arg0);
        if(convertView==null)
        {
            LayoutInflater inflater = (LayoutInflater)  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, parent, false);

            mHolder = new ViewHolder();

            mHolder.rName = (TextView) convertView.findViewById(R.id.text_route_name);
            mHolder.rStatus = (ImageView) convertView.findViewById(R.id.img_route_status);
            mHolder.rArea = (LinearLayout) convertView.findViewById(R.id.route_names_area);

            convertView.setTag(mHolder);
        }
        else{
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.rName.setText(rItem.getRouteName());
        mHolder.rArea.removeAllViews();
        setRouteNameList(rItem, mHolder.rArea);
        if(rItem.getRouteStatus()==RouteItem.ROUTE_COMPLETE)
            mHolder.rStatus.setImageResource(android.R.drawable.presence_online);
        else
            mHolder.rStatus.setImageResource(android.R.drawable.presence_offline);


        return convertView;
    }

    //create and bind nodename to a list of textview of the route
    public void setRouteNameList(RouteItem rItem, LinearLayout area){

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(5, 15, 0, 0);
        params.gravity = Gravity.LEFT;

        Cursor cursor = dbAdapter.getRouteInfoById(rItem.getId());
        if(cursor.moveToFirst()){
            do{
                String nodeName = cursor.getString(
                        cursor.getColumnIndex(MUVisitorDBAdapter.NODE_NAME)
                );

                TextView temp = new TextView(context);
                temp.setLayoutParams(params);
                temp.setText(nodeName);
                temp.setTextColor(Color.parseColor("#96F3EEF5"));
                temp.setId(View.generateViewId());
                area.addView(temp);
            }while(cursor.moveToNext());
        }
    }

    //create a list of Route from db
    private void populateRouteNames(){
        Cursor cursor = dbAdapter.listAllRoutes();
        cursor.requery();
        if(cursor.moveToFirst()){
            do{
                String route = cursor.getString(
                        cursor.getColumnIndex(MUVisitorDBAdapter.ROUTE_NAME)
                );
                int id = cursor.getInt(
                        cursor.getColumnIndex(MUVisitorDBAdapter.ROUTE_ID)
                );
                String status = cursor.getString(
                        cursor.getColumnIndex(MUVisitorDBAdapter.ROUTE_STATUS)
                );

                RouteItem routeItem;
                if(status.equals("COMPLETE"))
                    routeItem = new RouteItem(id,route,RouteItem.ROUTE_COMPLETE);
                else
                    routeItem = new RouteItem(id,route,RouteItem.ROUTE_INCOMPLETE);

                routeItemList.add(routeItem);
            }while(cursor.moveToNext());
        }
    }

    //handle view of listview
    private class ViewHolder {
        private TextView rName;
        private ImageView rStatus;
        private LinearLayout rArea;

    }
}
