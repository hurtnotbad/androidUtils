package com.example.lammy.androidutils.applictaion;

import android.app.Application;

import com.example.lammy.androidutils.base.Constants;
import com.example.lammy.androidutils.exception.CrashHandler;
import com.lzy.okgo.OkGo;

/**
 * Created by lammy on 2018/4/19.
 *
 * Application 再程序创建时候创建，需要再清单文件中配置，如本应用再application中添加]
 *  android:name=".applictaion.MyApplication"
 */

public class MyApplication extends Application {
    private static MyApplication mApplication = null;

    public static MyApplication getInstance(){
        return mApplication;
    }

    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        registerUncaughtExceptionHandler();
        OkGo.init(mApplication);
        OkGo.getInstance().setConnectTimeout(Constants.REQUEST_TIME_OUT)
                .setReadTimeOut(Constants.REQUEST_TIME_OUT)
                .setWriteTimeOut(Constants.REQUEST_TIME_OUT)
                .setCertificates()
                .setRetryCount(0);
    }

    // 注册App异常崩溃处理器
    private void registerUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }

}
