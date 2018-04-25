package com.example.lammy.androidutils.share;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.log.LogUtil;

import java.io.File;

public class ShareActivity extends AppCompatActivity {

    private Button shareButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        shareButton = findViewById(R.id.share_bt);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    public void share (){
        LogUtil.e("分享");
        ShareUtils.shareImageFromSdCard(this,"分享" , "分享功能测试" ,
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +"ID_Photo/cash_A.jpg");
    }

}
