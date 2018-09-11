package com.example.lammy.androidutils.Bitmap;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.example.lammy.androidutils.log.LogUtil;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by lammy on 17-8-25.
 */

public class BitmapUtil {



    /****************************loadBitmap*******************************************/
    public static Bitmap loadBitmapFromDrawable(Context context,int resId){
        InputStream is = context.getResources().openRawResource(resId);
        Bitmap mBitmap = BitmapFactory.decodeStream(is);
        //使用BitmapFactory.decodeResource()会根据资源目录对图像进行缩放，导致图像失真
        return mBitmap;
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap loadImageFromAssetsFile(Context context, String fileName, int scale) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            image = BitmapFactory.decodeStream(is, null, options);

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap loadImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    /**
     * 从sd卡读取小图,不加载原图到内存，效率高
     */
    public  Bitmap loadSmallBitmap(String imagePath ,int newHeight , int newWidth)
    {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;//(设为true 图片不加入内存效率高)
        BitmapFactory.decodeFile(imagePath , options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        System.out.println("jpg图"+outHeight+","+outWidth);
        //经实验证明，缩小的倍数只能是整数倍。
        options.inSampleSize=(outHeight/newHeight+outWidth/newWidth)/2;
        options.inJustDecodeBounds=false;
        Bitmap newBitmap = BitmapFactory.decodeFile(imagePath, options);
        return newBitmap;
    }
    public  Bitmap loadSmallBitmap(String imagePath ,int smallSize)
    {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;//(设为true 图片不加入内存效率高)
        BitmapFactory.decodeFile(imagePath , options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        System.out.println("jpg图"+outHeight+","+outWidth);
        //经实验证明，缩小的倍数只能是整数倍。
        options.inSampleSize=smallSize;
        options.inJustDecodeBounds=false;
        Bitmap newBitmap = BitmapFactory.decodeFile(imagePath, options);
        return newBitmap;
    }




    /**
     * 通过uri获取文件的路径
     * @param uri 选中相册的图片的uri
     * @param context
     * @return filePath 文件的路径
     */
    public static String getFilePathFromContentUri(Uri uri, Context context) {
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        }else {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return filePath;

    }

    /**
     * 本次查询的就是针对 相机里面的图片进行搜查,获得最近一排的一张照片,的路径
     * @param context
     * @return
     */
    public static String getLastPhotoByPath(Context context) {

        Cursor myCursor = null;
        String pathLast="";
        // Create a Cursor to obtain the file Path for the large image
        String[] largeFileProjection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.ORIENTATION,
                MediaStore.Images.ImageColumns.DATE_TAKEN };
        String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
        myCursor =
//                  BaseApplication.getInstance().
                context.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        largeFileProjection, null, null, largeFileSort);

        if (myCursor.getCount()<1) {
            myCursor.close();
            return pathLast;
        }
        while (myCursor.moveToNext()) {
            String data = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            File f = new File(data);
            if (f.exists()) {//第一个图片文件，就是最近一次拍照的文件；
                pathLast=f.getPath();
                LogUtil.e(" ==== ", "f.getPath() = "+pathLast);
                myCursor.close();
                return pathLast;
            }
        }
        myCursor.close();
        return pathLast;

    }

/****************************resizeBitmap*******************************************/

    /**
     * 调用ThumbnailUtils，效率封装比matrix高一点
     * @param bitmap
     * @param newHeight
     * @param newWidth
     * @return
     */
    public  Bitmap resizeBitmap(Bitmap bitmap ,int newHeight , int newWidth)
    {
        Bitmap newBitmap = ThumbnailUtils.extractThumbnail(bitmap,newWidth,newHeight);
        return   newBitmap;
    }
    /**
     * 等比率缩放图片，最终图像不一定都满足tarWidth和tarHeight
     * @param bitmap
     * @param tarWidth
     * @param tarHeight
     * @return
     */
    public static Bitmap resizeBitmapByMatrix(Bitmap bitmap,int tarWidth,int tarHeight) {
        Matrix matrix = new Matrix();
        float scaleWidth = tarWidth*1.0f/bitmap.getWidth();
        float scaleHeight = tarHeight*1.0f/bitmap.getHeight();
        float scale = Math.min(scaleWidth, scaleHeight);
        matrix.postScale(scale, scale);
        bitmap = Bitmap.createBitmap(bitmap,0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return bitmap;
    }
    /**
     * 利用矩阵等比率缩放图像
     * @param bitmap
     * @param scale
     * @return
     */
    public static Bitmap resizeBitmap(Bitmap bitmap,float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }


/****************************rotateBitmap*******************************************/

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param angle  旋转角度，可正可负
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap origin, float angle) {
        if (origin == null) {
            return null;
        }
        if(angle == 0){
            return  origin;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        return newBM;
    }

/****************************saveBitmap*******************************************/

    /**
     * 如果提供的存储目录不存在，则会创建，再存储 ，存储完后通知系统相册跟新
     * @param context
     * @param picPath
     * @param picName
     * @param bm
     * @param isUpdate 如果文件存在是否替换
     * @return
     */
    public static String saveBitmap(Context context, String picPath,
                                    String picName,Bitmap bm, boolean isUpdate) {

        File f = new File(picPath, picName);
        if(!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        if (f.exists()) {
            if(isUpdate){
                f.delete();
            }else {
                return f.getAbsolutePath();
            }
        }

        try {
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            // 通知图库
            MediaScannerConnection.scanFile(context,
                    new String[]{f.getAbsolutePath()}, null, null);

            return f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 同上一个方法，只不过默认替换
     * @param context
     * @param picPath
     * @param picName
     * @param bm
     * @return
     */
    public static String saveBitmap(Context context, String
            picPath, String picName,Bitmap bm) {
        return saveBitmap(context, picPath, picName, bm, true);
    }

    /**
     * 同上，只不过缺乏context无法通知系统更新目录
     * @param pathName
     * @param bm
     * @param isUpdate
     * @return
     */
    public static boolean saveBitmap(String pathName,Bitmap bm, boolean isUpdate) {

        File f = new File(pathName);
        if(!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        if (f.exists()) {
            if(isUpdate){
                f.delete();
            }else {
                return true;
            }
        }

        try {
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public static boolean saveBitmap(String pathName,Bitmap bm) {
        return saveBitmap(pathName, bm, true);
    }

    /**
     *
     * @param pathName
     * @param bm
     * @param isUpdate 是否覆盖
     * @param saveImageSuccessListener 保存完毕后的回调
     */
    public static void saveBitmap(final String pathName, final Bitmap bm, final boolean isUpdate , final SaveImageSuccessListener saveImageSuccessListener){
        new Thread(){
            @Override
            public void run() {
                File f = new File(pathName);
                if(!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                if (f.exists()) {
                    if(isUpdate){
                        f.delete();
                    }
                }
                try {
                    f.createNewFile();
                    FileOutputStream out = new FileOutputStream(f);
                    bm.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    if(saveImageSuccessListener != null){
                        saveImageSuccessListener.saveImageSuccess();
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();


    }

    public interface SaveImageSuccessListener{
        void saveImageSuccess();
    }




/****************************deleteBitmap*******************************************/
    /**
     * 根据图片文件路径删除图片文件
     * @param filePath
     * @return
     */
    public static boolean removeImageFile(Context context, String filePath){
        if(filePath == null || filePath.isEmpty()){
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
        return file.delete();
    }



    public static int readPictureDegree(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
        }
        return degree;
    }






}
