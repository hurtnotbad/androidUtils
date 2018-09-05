package com.example.lammy.androidutils.appIn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.log.LogUtil;
import com.example.lammy.androidutils.permission.PermissionActivity;
import com.example.lammy.androidutils.permission.Permissions;


public class MainActivity extends PermissionActivity {
    private GridView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGridView = findViewById(R.id.grid_view);mGridView.setColumnWidth(200);
        final FunctionAdapter functionAdapter = new FunctionAdapter(this);
        mGridView.setAdapter( functionAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String functionName = functionAdapter.getFunctionName(position);
                functionAdapter.gotoFunction(functionName);
            }
        });

//        Bitmap bitmap =  Bitmap.createBitmap(1000,2333, Bitmap.Config.ARGB_8888);
//        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.timg);
//
//        Bitmap bitmap3 = bitmap2;
//        bitmap2 = bitmap;
//        if(bitmap2 == bitmap3)
//        LogUtil.e("bitmap2=bitmap3");
//        else {
//            LogUtil.e("bitmap2!=bitmap3");
//        }
    }







}
