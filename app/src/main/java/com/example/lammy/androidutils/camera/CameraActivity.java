package com.example.lammy.androidutils.camera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.permission.Permissions;

public class CameraActivity extends AppCompatActivity {

    private CameraView camera_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

    }
}
