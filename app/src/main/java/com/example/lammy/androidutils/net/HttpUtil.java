package com.example.lammy.androidutils.net;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Toast;

import com.example.lammy.androidutils.applictaion.MyApplication;
import com.example.lammy.androidutils.log.LogUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.download.DownloadManager;
import com.lzy.okserver.download.DownloadService;
import com.lzy.okserver.listener.DownloadListener;

/**
 * Created by zhangpeng30 on 2018/4/24.
 *
 * 用到了开源框架okgo，在gradle中添加相应的依赖库
 * compile 'com.lzy.net:okgo:2.1.4'
 * 可以单独使用，不需要依赖下方的扩展包
 * compile 'com.lzy.net:okserver:1.1.3'
 * 下载管理和上传管理扩展，根据需要添加
 *
 * 具体参考：https://blog.csdn.net/u012426327/article/details/78677640#t23
 *
 *
 * 使用：
 * 1、在application中初始化
 *
 */

public class HttpUtil {
    public static final String url_host = "http://10.5.11.136:8080/idphoto/";
    public static final String url_image = url_host + "lyf.jpg";

    private static HttpUtil mHttpUtil;
    private static Context mContext;

    public static HttpUtil getInstance() {
        if(mHttpUtil!=null){
            return mHttpUtil;
        }
        synchronized (HttpUtil.class){
            if (mHttpUtil == null) {
                mHttpUtil = new HttpUtil();
            }
        }
        mContext = MyApplication.getInstance();
        return mHttpUtil;
    }

    public void downloadFile(String url, Object tag, AbsCallback callback){
        if (!NetUtil.isConnectedNet(mContext)) {
//            ToastUtil.showLong(mContext, "您的网络异常");
            DisplayMetrics dm2 = mContext.getResources().getDisplayMetrics();
            Toast toast = Toast.makeText(mContext, "您的网络异常", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0,  dm2.heightPixels/4);
            toast.show();

            LogUtil.e( "没有网络");
            return;
        }
        OkGo.get(url)
                .tag(tag)
                .execute(callback);
    }

    /**
     * DownloadManager 异步下载文件，支持断点下载
     * @param url 网络url
     * @param fileTargetFolder 文件下载路径（文件夹）
     * @param downloadListener 下载完成的监听器
     */
    public void doDownload(String url, String fileTargetFolder , DownloadListener downloadListener) {
        DownloadManager dm = DownloadService.getDownloadManager();
        dm.setTargetFolder(fileTargetFolder);
        if(dm.getDownloadInfo(url) != null && dm.getDownloadInfo(url).getState() == DownloadManager.ERROR){
            dm.removeTask(dm.getDownloadInfo(url).getTaskKey());
        }
        GetRequest request = OkGo.get(url);
        dm.addTask(url, request, downloadListener);
    }


}
