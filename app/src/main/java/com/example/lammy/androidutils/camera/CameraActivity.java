package com.example.lammy.androidutils.camera;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.log.LogUtil;


public class CameraActivity extends AppCompatActivity {

    private  SurfaceView setSurfaceView;
    private lammyCamera2 camera_view;
    private Button bt_view,bt_changeCamera;
    private TextView fps_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setSurfaceView = findViewById(R.id.surface);
        camera_view = findViewById(R.id.camera_view);
        bt_view =  findViewById(R.id.bt_view);
        bt_changeCamera =  findViewById(R.id.bt_changeCamera);
        fps_view =  findViewById(R.id.fps_view);
        camera_view.setSurfaceView(setSurfaceView);
        camera_view.setFpsView(fps_view);
    }
    public void transfer(View view){
        LogUtil.e("lammy  transfer onClick ");
        if(camera_view.isTransferOpen()){
            bt_view.setText("transfer");
            camera_view.setTransfer(false);


        }else {
            bt_view.setText("切换到原预览");
            camera_view.setTransfer(true);

        }

    }

    public void changeCamera(View view){
        camera_view.changeCamera();
    }
    public void takePhoto(View view){
//        camera_view.takePhoto(true, Environment.getExternalStorageDirectory().getAbsolutePath()+"/alammy/", 1);
        camera_view.takePhoto(true,Environment.getExternalStorageDirectory().getAbsolutePath()+"/lammy/");
    }

    public void setFlashMode(View view){
//        camera_view.takePhoto(true,Environment.getExternalStorageDirectory().getAbsolutePath()+"/alammy/", 4);
//        camera_view.takePhoto(true);
        camera_view.setFlashMode(lammyCamera2.FLASH_ALWAYS);
        camera_view.setFlashMode(lammyCamera2.FLASH_SINGLE);

    }

}
