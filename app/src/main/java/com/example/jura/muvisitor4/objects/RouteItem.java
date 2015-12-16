package com.example.jura.muvisitor4.objects;

import android.widget.LinearLayout;

/**
 * Created by Jura on 5/6/2015.
 */
public class RouteItem {
    private int routeId;
    private String routeName;
    private int routeStatus;
    public LinearLayout area;
    public final static int ROUTE_INCOMPLETE = 0;
    public final static int ROUTE_COMPLETE = 1;

    public RouteItem(int routeId, String routeName, int routeStatus){
        this.routeId = routeId;
        this.routeName = routeName;
        this.routeStatus = routeStatus;
    }

    public int getId(){
        return routeId;
    }

    public String getRouteName(){
        return routeName;
    }

    public  int getRouteStatus(){
        return routeStatus;
    }
}
