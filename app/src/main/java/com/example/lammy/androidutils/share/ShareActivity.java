package com.example.lammy.androidutils.share;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.log.LogUtil;
import com.example.lammy.androidutils.permission.Permissions;

import java.io.File;

public class ShareActivity extends AppCompatActivity {

    private Button shareButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
//       if( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//           LogUtil.e("已经被授予权限了！");
//       }
//        Permissions.requestPermissions(this,null , Permissions.PERMISSION_REQUEST_CODE);
        shareButton = findViewById(R.id.share_bt);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void share (){
        LogUtil.e("分享");

        BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"ID_Photo/cash_A.jpg");
        ShareUtils.shareImageFromSdCard(this,"分享" , "分享功能测试" ,
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"ID_Photo/cash_A.jpg");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Permissions.PERMISSION_REQUEST_CODE){
            for(int result : grantResults){
                if(result ==  PackageManager.PERMISSION_DENIED)
                {
                    LogUtil.e("权限拒绝");
                    finish();
                }
            }
            LogUtil.e("申请成功！");
        }
    }
}
