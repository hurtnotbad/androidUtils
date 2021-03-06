package com.example.lammy.androidutils.permission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



public class PermissionActivity extends AppCompatActivity implements PermissionGrandListener{

    private String permissions[] = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    private final int REQUEST_CODE = 805;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissionGrant()) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }



    private boolean checkPermissionGrant(){
        int size = permissions.length;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < size; i++) {
                if (checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                   return false;
                }
            }
        }
        return   true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE) {
            for(int result:grantResults){
                if(result == PackageManager.PERMISSION_DENIED){
                   onGrandFailed();
                    return;
                }
            }
            onGrandSuccess();
        }

    }

    private String[] getRequestPermissions(){
      return   setRequestPermission();
    }

    @Override
    public String[] setRequestPermission() {
        return new String[0];
    }

    @Override
    public void onGrandSuccess() {

    }

    @Override
    public void onGrandFailed() {

    }
}

// interface PermissionGrandListener{
//    String[] setRequestPermission();
//    void onGrandSuccess();
//    void onGrandFailed();
//}