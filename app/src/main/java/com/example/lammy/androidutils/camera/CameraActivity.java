package com.example.lammy.androidutils.camera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

import com.example.lammy.androidutils.R;
import com.example.lammy.androidutils.permission.Permissions;

public class CameraActivity extends AppCompatActivity {

    private Camera2View camera_view;
    private SurfaceView surfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.surface);
        camera_view = findViewById(R.id.camera_view);
        camera_view.setSurfaceView(surfaceView);

    }
}
