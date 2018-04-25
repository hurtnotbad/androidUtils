package com.example.lammy.androidutils.applictaion;

import android.app.Application;

import com.example.lammy.androidutils.base.Constants;
import com.example.lammy.androidutils.exception.CrashHandler;
import com.lzy.okgo.OkGo;

/**
 * Created by zhangpeng30 on 2018/4/19.
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
        OkGo.init(this);
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
