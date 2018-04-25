package com.example.lammy.androidutils.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.lammy.androidutils.log.LogUtil;

/**
 * 网络连接状态的工具类
 * <p>
 * Created by lammy on 2017/2/28.
 */

public class NetUtil {

    /**
     * 判断网络是否连接
     * @param context
     * @return
     */
    public static boolean isConnectedNet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     * @param context
     * @return
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是数据网络连接
     * @param context
     * @return
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        if (networkINfo != null
                && networkINfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param context
     * @return 返回注册的监听器，方便后面注销监听
      */
    public static  BroadcastReceiver registerNetChangeListener(Context context){
        BroadcastReceiver NetChangeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(NetUtil.isWifi(context)) {
                        //wifi连接情况
                        LogUtil.e("Http-lammy" , "wifi连接上了 ,");
                    } else if(NetUtil.isMobile(context)){
                        //仅数据连接
                        LogUtil.e("Http-lammy" , "wifi断开了，手机数据网络连接");
                    }else{
                        //断网状态
                        LogUtil.e("Http-lammy" , "断网了");
                    }
                }

            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(NetChangeListener ,intentFilter );
        return NetChangeListener;
    }

}
