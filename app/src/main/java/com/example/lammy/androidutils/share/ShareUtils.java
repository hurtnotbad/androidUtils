package com.example.lammy.androidutils.share;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.widget.VideoView;

import com.example.lammy.androidutils.Bitmap.BitmapUtil;
import com.example.lammy.androidutils.base.Constants;
import com.example.lammy.androidutils.log.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**分享管理
 *
 * Created by madl on 2017/9/20.
 */

public class ShareUtils {
    private final static String  TAG = ShareUtils.class.getSimpleName();
    public static final int VERTICAL = 1;
    public static final  int HORIZONTAL = 2;
    public static final  int INSIDE = 3;
    /**
     * 调用系统的分享功能，分享图片
     * @param context
     * @param activityTitle 标题
     * @param msgText 信息
     * @param imagePath 图片位置（调用系统分享功能，不需要申请sd卡权限）
     */
    public static void shareImageFromSdCard(Context context, String activityTitle, String msgText, String imagePath ){
        // 启动分享发送的属性  
        Intent intent = new Intent(Intent.ACTION_SEND);
        //intent.setPackage("com.tencent.mm");
        if(imagePath == null && imagePath.equals("")){
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, msgText);
        }else {
            File file = new File(imagePath);
            if(file != null && file.exists() && file.isFile()){
                intent.setType("image/*");
                Uri uri = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);

            }else {
                return;
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, activityTitle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, activityTitle));
    }

    /**
     * 这里是先将assets里面图片保存到sd卡，然后进行分享，因此这里需要权限
     * @param context
     * @param activityTitle
     * @param fileName
     */
    public static void shareImageFromAssets(Context context, String activityTitle, String fileName ){
        // 启动分享发送的属性  
        if(fileName != null && !fileName.equals("")){
            Bitmap bitmap = BitmapUtil.loadImageFromAssetsFile(context, fileName);
            Uri uri = saveBitmap(context, bitmap, Constants.lammy_utils + fileName);
            if(uri != null){
                Intent intent = new Intent(Intent.ACTION_SEND);
                //intent.setPackage("com.tencent.mm");
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, activityTitle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(Intent.createChooser(intent, activityTitle));
            }
        }
    }

    public static void openShare(Context context, String activityTitle, String fileName, Bitmap bitmap ){
        // 启动分享发送的属性  
        if(fileName != null && !fileName.equals("")){
            Uri uri = saveBitmap(context, bitmap, Constants.lammy_utils + fileName);
            if(uri != null){
                Intent intent = new Intent(Intent.ACTION_SEND);
                //intent.setPackage("com.tencent.mm");
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, activityTitle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(Intent.createChooser(intent, activityTitle));
            }
        }
    }

    /**
     * 通过view 生成一张界面图片
     * @param view
     * @param imagePath
     */
    public static void saveImageFileByView(View view, String imagePath , VideoView videoView, String filePath) {
        View rootView =  view.getRootView();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache();
        Bitmap bimap = rootView.getDrawingCache();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(filePath);
        Bitmap videoBitmap = retriever.getFrameAtTime(videoView.getCurrentPosition() * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        if(videoBitmap == null){
            return;
        }

        int [] location = new int[2];
        videoView.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];

        Rect src = new Rect();
        src.left = 0;
        src.top = 0;
        src.right = videoBitmap.getWidth();
        src.bottom = videoBitmap.getHeight();
        Rect dst = new Rect();
        dst.left = x;
        dst.top = y;
        dst.right = x + videoView.getWidth();
        dst.bottom = y + videoView.getHeight();

        retriever.release();
        Canvas canvas = new Canvas(bimap);
        canvas.drawBitmap(videoBitmap, src, dst, new Paint());
        canvas.save();
        canvas.restore();

        if(bimap != null){
            try {
                FileOutputStream out = new FileOutputStream(imagePath);
                bimap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getViewBitmap(View v) {
        if (null == v) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        if (Build.VERSION.SDK_INT >= 11) {
            v.measure(View.MeasureSpec.makeMeasureSpec(v.getWidth(),
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                    v.getHeight(), View.MeasureSpec.EXACTLY));
            v.layout((int) v.getX(), (int) v.getY(),
                    (int) v.getX() + v.getMeasuredWidth(),
                    (int) v.getY() + v.getMeasuredHeight());
        } else {
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache(), 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        v.setDrawingCacheEnabled(false);
        v.destroyDrawingCache();
        return b;
    }

    public static void screenshotByWindow(Window window, String imagePath){
        View dView = window.getDecorView();
        dView.setDrawingCacheEnabled(true);
        dView.buildDrawingCache();
        Bitmap bimap = dView.getDrawingCache();
        if(bimap != null){
            try {
                FileOutputStream out = new FileOutputStream(imagePath);
                bimap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                LogUtil.e(TAG, " bimap  " + bimap.getConfig());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 按比例缩放图片
     *
     * @param origin 原图
     * @param ratio  比例
     * @return 新的bitmap
     */
    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    /** * 将图片存到本地 */
    private static Uri saveBitmap(Context context, Bitmap bm, String filePath) {
        try {
            String dir = filePath;
            File f = new File(dir);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
//            Uri uri = Uri.fromFile(f);
            Uri uri = null;
            try{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    uri = FileProvider.getUriForFile(context, "com.motorola.idphoto.fileprovider", f);
                }else{
                    uri = Uri.fromFile(f);
                }
            }catch (ActivityNotFoundException anfe){
                LogUtil.e(TAG,anfe.getMessage());
            }
//            Uri uri = FileProvider.getUriForFile(context, "com.motorola.idphoto.fileprovider", f);
            MediaScannerConnection.scanFile(context, new String[]{f.getAbsolutePath()}, null, null);
            return uri;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();    }
        return null;
    }


}
