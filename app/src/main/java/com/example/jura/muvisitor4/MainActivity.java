package com.example.jura.muvisitor4;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.jura.muvisitor4.objects.RouteItem;
import com.example.jura.muvisitor4.services.MUVisitorDBAdapter;
import com.example.jura.muvisitor4.services.RouteListAdapter;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    final static String ROUTE_NODE_INTENT = "route_node_intent";
    final static String ROUTE_ID_INTENT = "route_id_intent";
    final static String NODE_ID_INTENT = "node_id_intent";
    final static String NODE_NUMBER_INTENT = "node_no_intent";
    ListView routeListView;
    ArrayList<RouteItem> routeItems;
    RouteListAdapter adapter;
    Cursor cursor;
    private MUVisitorDBAdapter dbAdapter = new MUVisitorDBAdapter(this);
    AlertDialog.Builder proceedDialog;
    int lastRouteClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        this.deleteDatabase("MUVisitor.db");

        //Init DB
        dbAdapter.open();

        //UI
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar ().setIcon(R.mipmap.icon_photoloco);
        routeListView = (ListView) findViewById(R.id.routeListView);



        proceedDialog = new AlertDialog.Builder(this)
                .setTitle("Choose this route?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        toMapActivity(lastRouteClicked);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);
        routeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)  {
                int routeId = adapter.routeItemList.get(position).getId();
                proceedDialog.setMessage(buildRouteInfo(routeId));
                proceedDialog.show();
                lastRouteClicked = routeId;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter = new RouteListAdapter(this, R.layout.route_item_layout, dbAdapter);
        routeListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Create String to display in alert dialog
    //The info is node list in that route
    private String buildRouteInfo(int routeId){
        Cursor cursor = dbAdapter.getRouteInfoById(routeId);
        String info = "";
        if(cursor.moveToFirst()){
            int count = 1;
            do{
                String nodeName = cursor.getString(
                        cursor.getColumnIndex(MUVisitorDBAdapter.NODE_NAME)
                );
                info += count + ". " + nodeName + "\n";
                count++;
            }while(cursor.moveToNext());
        }
        return info;
    }

    //intent to MapActivity
    private void toMapActivity(int routeId){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(ROUTE_ID_INTENT, Integer.toString(routeId));
        startActivity(intent);
    }
}
