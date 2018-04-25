package com.example.lammy.androidutils.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.base.Constants;
import com.example.lammy.androidutils.log.LogUtil;
import com.lzy.okserver.download.DownloadInfo;
import com.lzy.okserver.listener.DownloadListener;

public class DownloadActivity extends AppCompatActivity {

    private ImageView mImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mImageView = findViewById(R.id.image_view);
        LogUtil.e("download activity...");

    }
    public void download(View view){
        LogUtil.e("download ...");
        HttpUtil httpUtil =  HttpUtil.getInstance();
        httpUtil.doDownload(HttpUtil.url_image, Constants.lammy_utils, new DownloadListener() {
            @Override
            public void onProgress(DownloadInfo downloadInfo) {

            }

            @Override
            public void onFinish(DownloadInfo downloadInfo) {

               final String imagePath = Constants.lammy_utils + downloadInfo.getFileName();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                        mImageView.setImageBitmap(bitmap);
                    }
                });
            }

            @Override
            public void onError(DownloadInfo downloadInfo, String errorMsg, Exception e) {

            }
        });
    }

}
