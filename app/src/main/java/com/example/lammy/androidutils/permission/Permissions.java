package com.example.lammy.androidutils.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.example.lammy.androidutils.log.LogUtil;

/**
 * Created by zhangpeng30 on 2018/4/25.
 *
 * 部分手机都是在运行时候申请权限，并非是你申请了权限就弹出对话框让授权，而是打开要用到相应权限的activity才会申请授权
 *
 * 部分手机则是申请就马上弹框
 *
 * 部分手机无论在哪授权，在打开应用时候就申请所有权限
 *
 * 此处提供2种方式
 * 1、调用本类方法申请权限，在相应的activity种调用onRequestPermissionsResult 回调来处理
 * 2、让主activity 继承写的PermissionActivity即可
 */

public class Permissions {
    public static String  writeSDCardPermission=  Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static String readSDCardPermission =  Manifest.permission.READ_EXTERNAL_STORAGE;
    public static String cameraPermission =  Manifest.permission.CAMERA;

    public static int PERMISSION_REQUEST_CODE= 0;
    public static  int PERMISSION_DENIED = PackageManager.PERMISSION_DENIED;
    public static  int PERMISSION_GRANTED= PackageManager.PERMISSION_GRANTED;

    public static void requestPermissions(Activity activity , String[] permissions , int RequestCode)
    {
        if(permissions == null){
            LogUtil.e("申请权限");
            ActivityCompat.requestPermissions(activity , new String[]{writeSDCardPermission , readSDCardPermission , cameraPermission} , RequestCode);
        } else {
            ActivityCompat.requestPermissions(activity ,permissions , RequestCode);
        }
    }

}
