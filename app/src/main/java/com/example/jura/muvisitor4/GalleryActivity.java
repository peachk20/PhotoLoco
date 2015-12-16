
//http://javatechig.com/android/android-gridview-example-building-image-gallery-in-android
package com.example.jura.muvisitor4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.jura.muvisitor4.services.GalleryAdapter;
import com.example.jura.muvisitor4.objects.ImageItem;
import com.example.jura.muvisitor4.services.MUVisitorDBAdapter;

import java.io.File;
import java.util.ArrayList;

import static com.example.jura.muvisitor4.services.MUVisitorDBAdapter.*;


public class GalleryActivity extends ActionBarActivity {

    private int routeId;

    //UI
    private GridView gridGallery;
    private GalleryAdapter gridAdapter;


    //DB
    private MUVisitorDBAdapter dbAdapter = new MUVisitorDBAdapter(this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //DB
        dbAdapter.open();

        routeId = getIntent().getIntExtra(MainActivity.ROUTE_ID_INTENT,0);
        if(routeId == 0){
            toast("Fail: route id = 0");
            backToMap();
        }
        else{

            setTitle(dbAdapter.getRouteName(routeId));
            gridGallery = (GridView) findViewById(R.id.grid_gallery);
            gridAdapter = new GalleryAdapter(this, R.layout.grid_item_layout, getImageItems());
            gridGallery.setAdapter(gridAdapter);
            gridGallery.setOnItemClickListener(gridGalleryListener);
            gridGallery.setOnItemLongClickListener(gridGalleryLongclickListener);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //get array of images to use in adapter
    private ArrayList<ImageItem> getImageItems(){
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        Cursor cursor = dbAdapter.getPhotoByRouteId(this.routeId);
        if(cursor.moveToFirst()){
            do{
                int nodeId = cursor.getInt(
                        cursor.getColumnIndex(NODE_ID)
                );
                int photoId = cursor.getInt(
                        cursor.getColumnIndex(PHOTO_ID)
                );
                String photoPath = cursor.getString(
                        cursor.getColumnIndex(PHOTO_PATH)
                );
                String nodeName = cursor.getString(
                        cursor.getColumnIndex(NODE_NAME)
                );

                imageItems.add(new ImageItem(
                        nodeName,
                        photoPath,
                        nodeId,
                        photoId
                ));
            }while(cursor.moveToNext());
        }
        return imageItems;
    }

    //go to SingleImage activity
    private AdapterView.OnItemClickListener gridGalleryListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ImageItem item = (ImageItem) parent.getItemAtPosition(position);
            //Create intent
            Intent intent = new Intent(getBaseContext(), SingleImageActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("image", item.getPath());

            //Start details activity
            startActivity(intent);
        }
    };

    //back to Map activity
    private void backToMap(){
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
    }

    //popup dialog to delete image
    private AdapterView.OnItemLongClickListener gridGalleryLongclickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

            new AlertDialog.Builder(GalleryActivity.this)
                    .setTitle("Delete entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ImageItem img = gridAdapter.getItem(position);
                            File file = new File(img.getPath());
                            if(file.delete()){
                                gridAdapter.removeItem(position);
                                dbAdapter.deletePhotoById(img.getId());
                                dbAdapter.updateRouteStatus(routeId);
                            }
                            else{
                                toast("Fail to delete: "+img.getPath());
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        }
    };

    protected void toast(String s){
        Toast.makeText(getBaseContext(), s, Toast.LENGTH_SHORT).show();
    }

}
