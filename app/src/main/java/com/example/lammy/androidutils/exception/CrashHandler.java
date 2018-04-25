package com.example.lammy.androidutils.exception;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;

import com.example.lammy.androidutils.applictaion.MyApplication;
import com.example.lammy.androidutils.base.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 在applictation 内使用，当程序崩溃时候就会保存log到指定文件夹
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler5_1";
    public static final String CRASH_FILE_PATH = Constants.lammy_utils;
    private static Map<String, String> info;
    private SimpleDateFormat format;
    public CrashHandler() {
        info = new HashMap<>();
        format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
//        try {
//            TelephonyManager tm = (TelephonyManager) MyApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
////            imei = tm.getDeviceId();
//        } catch (Exception e) {
////            imei = "No_permission_to_get_device_ID";
//        }
    }

//    public static CrashHandler getInstance() {
//        return instance;
//    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        System.exit(0);
    }

    /**
     * 用于处理Crash
     **/
    public boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        // 收集设备参数信息
        collectDeviceInfo(MyApplication.getInstance());
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     */
    public void collectDeviceInfo(Context context) {

        info.put("SDK version", String.valueOf(Build.VERSION.SDK_INT));
        info.put("android version", Build.VERSION.RELEASE);
        info.put("total_time", getTimes());
//        info.put("imei", imei);
        try {
            PackageManager pm = context.getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("soft_ware_name", versionName);
                info.put("soft_ware_version", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.getName().equals("TIME")) {
                    info.put("crash_time", format.format(new Date(System.currentTimeMillis())));
                } else {
                    info.put(field.getName(), field.get("").toString());
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + ":" + value + "\r\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// 记得关闭
        String result = writer.toString();
        sb.append(result);

        // 保存文件
        String time = format.format(new Date());
        String fileName = MyApplication.getInstance().getPackageName() + "-" + time + ".txt";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(CRASH_FILE_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
                fos.write(sb.toString().getBytes());
                fos.close();
//                Intent intent = new Intent();
//                ComponentName componentName = new ComponentName(context,CrashDealService.class);
//                intent.setComponent(componentName);
//                intent.putExtra("logPath",CRASH_FILE_PATH +File.separator + fileName);
//                context.startService(intent);

                return fileName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /***
     * 获取开机时长
     ****/
    private String getTimes() {
        long ut = SystemClock.elapsedRealtime() / 1000;
        if (ut == 0) {
            ut = 1;
        }
        int s = (int) (ut % 60);
        int m = (int) ((ut / 60) % 60);
        int h = (int) ((ut / 3600));
        return h + "h  " + m + "m  " + s + "s";
    }
}
