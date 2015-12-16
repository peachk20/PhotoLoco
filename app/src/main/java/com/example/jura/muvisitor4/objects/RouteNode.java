package com.example.jura.muvisitor4.objects;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.jura.muvisitor4.services.MUVisitorDBAdapter;

/**
 * Created by Jura on 4/19/2015.
 */
public class RouteNode implements Parcelable{
    public int nodeId;
    public String nodeName;
    public Double lat;
    public Double lng;
    public int nodePrev;
    public int nodeNext;
    public int routeId;

    public RouteNode(Cursor cursor){

        if(cursor.moveToFirst()) {
            nodeId = cursor.getInt(
                    cursor.getColumnIndex(MUVisitorDBAdapter.NODE_ID)
            );
            nodeName = cursor.getString(
                    cursor.getColumnIndex(MUVisitorDBAdapter.NODE_NAME)
            );
            lat = cursor.getDouble(
                    cursor.getColumnIndex(MUVisitorDBAdapter.NODE_LAT)
            );
            lng = cursor.getDouble(
                    cursor.getColumnIndex(MUVisitorDBAdapter.NODE_LNG)
            );
            nodePrev = cursor.getInt(
                    cursor.getColumnIndex(MUVisitorDBAdapter.NODE_PREV)
            );
            nodeNext = cursor.getInt(
                    cursor.getColumnIndex(MUVisitorDBAdapter.NODE_NEXT)
            );
            routeId = cursor.getInt(
                    cursor.getColumnIndex(MUVisitorDBAdapter.ROUTE_ID)
            );
        }
    }


    /**** Making RouteNode become parcable ***/
    private int mData;

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    public static final Parcelable.Creator<RouteNode> CREATOR
            = new Parcelable.Creator<RouteNode>() {
        public RouteNode createFromParcel(Parcel in) {
            return new RouteNode(in);
        }

        public RouteNode[] newArray(int size) {
            return new RouteNode[size];
        }
    };

    /** recreate object from parcel */
    private RouteNode(Parcel in) {
        mData = in.readInt();
    }
}
