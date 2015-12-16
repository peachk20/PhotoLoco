package com.example.jura.muvisitor4.services;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jura.muvisitor4.R;
import com.example.jura.muvisitor4.objects.ImageItem;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Jura on 4/27/2015.
 */
public class GalleryAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();

    public GalleryAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        ImageItem item = (ImageItem) data.get(position);
        holder.imageTitle.setText(item.getTitle());
        loadBitmap(item,holder.image);
        return row;
    }

    public ImageItem getItem(int pos){
        return (ImageItem) data.get(pos);
    }
    public void removeItem(int pos){
        data.remove(pos);
        this.notifyDataSetChanged();
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }

    //do asyncable when load image for performance
    public void loadBitmap(ImageItem img , ImageView imageView) {

        if (BitMapLoaderTask.cancelPotentialWork(img, imageView)) {
            final BitMapLoaderTask task = new BitMapLoaderTask(imageView);
            final BitMapLoaderTask.AsyncDrawable asyncDrawable =
                    new BitMapLoaderTask.AsyncDrawable(this.getContext().getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(img);
        }
    }

}