package com.example.jura.muvisitor4.services;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.example.jura.muvisitor4.objects.ImageItem;

import java.lang.ref.WeakReference;

/**
 * Created by Jura on 4/29/2015.
 */
class BitMapLoaderTask extends AsyncTask<ImageItem, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    public ImageItem data = null;

    public BitMapLoaderTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(ImageItem... params) {
        data = params[0];
        return data.getRotatedThumbnail();
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }


    }

    //cancel loading bitmap, called when leaving gallery activity
    public static boolean cancelPotentialWork(ImageItem data, ImageView imageView) {
        final BitMapLoaderTask bitmapWorkerTask = getBitmapLoaderTask(imageView);

        if (bitmapWorkerTask != null) {
            final ImageItem imgData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (imgData == null || imgData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    private static BitMapLoaderTask getBitmapLoaderTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapLoaderTask();
            }
        }
        return null;
    }
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitMapLoaderTask> bitmapLoaderTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitMapLoaderTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapLoaderTaskReference =
                    new WeakReference<BitMapLoaderTask>(bitmapWorkerTask);
        }

        public BitMapLoaderTask getBitmapLoaderTask() {
            return bitmapLoaderTaskReference.get();
        }
    }
}