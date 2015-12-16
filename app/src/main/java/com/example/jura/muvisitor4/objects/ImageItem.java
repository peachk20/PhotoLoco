package com.example.jura.muvisitor4.objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;

/**
 * Created by Jura on 4/27/2015.
 */


public class ImageItem {
    private int photoId;
    private Bitmap image;
    private Bitmap thumbnail;
    private String title;
    private String photoPath;
    private int nodeId;
    private final int WIDTH_SCALE = 100;
    private final int HEIGHT_SCALE = 100;

    public ImageItem(String title, String photoPath, int nodeId, int photoId) {
        super();
        this.title = title;
        this.photoPath = photoPath;
        this.nodeId = nodeId;
        this.photoId = photoId;
    }

    public ImageItem(String photoPath){
        super();
        this.photoPath = photoPath;
    }

    public Bitmap getImage() {
        return decodeSampledBitmap(1024,1024);
    }
    public Bitmap getThumbnail() {
        return decodeSampledBitmap(128,128);
    }
    public void setImage(Bitmap image) {
        this.image = image;
    }

    public int getId() {
        return this.photoId;
    }
    public String getTitle() {
        return title;
    }
    public String getPath() {
        return photoPath;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Caluculate Sampling Size: Official Android Dev
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Sampling Bitmap: Official Android Dev
    public Bitmap decodeSampledBitmap(int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(this.photoPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(this.photoPath, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    // Rotate Function: Official Android Dev
    public Bitmap getRotatedImage(){
        try{
            ExifInterface exif = new ExifInterface(this.photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            return ImageItem.rotateBitmap(this.getImage(), orientation);
        }
        catch(IOException e){
            return null;
        }
    }

    public Bitmap getRotatedThumbnail(){
        try{
            ExifInterface exif = new ExifInterface(this.photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            return ImageItem.rotateBitmap(this.getThumbnail(), orientation);
        }
        catch(IOException e){
            return null;
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        try{
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return bitmap;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return bitmap;
            }
            try {
                Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return bmRotated;
            }
            catch (OutOfMemoryError e) {
                e.printStackTrace();
                return null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}