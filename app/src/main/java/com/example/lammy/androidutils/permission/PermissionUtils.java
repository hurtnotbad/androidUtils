package com.example.lammy.androidutils.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionUtils  {

    private Activity activity;
    PermissionGrandListener permissionGrandListener;

    public PermissionUtils(Activity activity, PermissionGrandListener permissionGrandListener){
        this.activity = activity;
        this.permissionGrandListener = permissionGrandListener;
    }


    private boolean checkPermissionGrant(){
       String[] permissions = getRequestPermissions();
        int size = permissions.length;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < size; i++) {
                if (activity.checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        }
        return   true;
    }

    private String[] getRequestPermissions(){
        if(permissionGrandListener != null)
        return   permissionGrandListener.setRequestPermission();
        else
            return null;
    }


}
interface PermissionGrandListener{
    String[] setRequestPermission();
    void onGrandSuccess();
    void onGrandFailed();
}