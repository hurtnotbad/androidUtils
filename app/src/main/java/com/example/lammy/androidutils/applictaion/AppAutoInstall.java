package com.example.lammy.androidutils.applictaion;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;


import com.example.lammy.androidutils.log.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AppAutoInstall {

	private final static String TAG = AppAutoInstall.class.getSimpleName();
	public static int modServiceApkVersion = 200010; //200010
	public static String modServicPacketName = "com.motorola.modservice";
	public static String leStoryPacketName = "com.lenovo.leos.appstore";

	/**
	 * 设置虚拟机堆大小以及回收
	 * @param size 堆大小
	 * @param f 回收方式
	 */
	public static void setMinHeapSize(long size, float f) {
		try {
			Class<?> cls = Class.forName("dalvik.system.VMRuntime");
			Method getRuntime = cls.getMethod("getRuntime");
			Object obj = getRuntime.invoke(null);// obj就是Runtime
			if (obj == null) {
				System.err.println("obj is null");
			} else {
				System.out.println(obj.getClass().getName());
				Class<?> runtimeClass = obj.getClass();
				Method setMinimumHeapSize = runtimeClass.getMethod(
						"setMinimumHeapSize", long.class);
				Method setTargetHeapUtilization = runtimeClass.getMethod(
						"setTargetHeapUtilization", long.class);
				setTargetHeapUtilization.invoke(obj, f);
				setMinimumHeapSize.invoke(obj, size);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取应用程序名称
	 */
	public static String getAppName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			return context.getResources().getString(labelRes);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取应用版本号
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionName;

		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getVersionCode(Context context) {
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获取手机的modservice版本号
	 * @param context
	 * @return 如过手机没安装modservice，则返回-1
	 */
	public static int getModServiceVersion(Context context){
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(AppAutoInstall.modServicPacketName,0);
			int versionCode = info.versionCode;
			String versionName = info.versionName;
			LogUtil.e(TAG,"versionName:" + versionName + ";versionCode:" + versionCode);
			return versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			LogUtil.e(TAG,"notfount:" + e.getMessage());
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * 判断是否存在
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isAvilible(Context context, String packageName) {
		final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		List<String> pName = new ArrayList<String>();// 用于存储所有已安装程序的包名
		// 从pinfo中将包名字逐一取出，压入pName list中
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				pName.add(pn);
			}
		}
		return pName.contains(packageName);// 判断pName中是否有目标程序的包名，有TRUE，没有FALSE
	}

	/**
	 * 启动到app详情界面
	 * @param appPkg    App的包名
	 * @param marketPkg 应用商店包名 ,如果为""则由系统弹出应用商店列表供用户选择,否则调转到目标市场的应用详情界面，某些应用商店可能会失败
	 */
	public static void launchAppDetail(Context context, String appPkg, String marketPkg) {
		try {
			if (TextUtils.isEmpty(appPkg))
				return;
			Uri uri = Uri.parse("market://details?id=" + appPkg);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			if (!TextUtils.isEmpty(marketPkg))
				intent.setPackage(marketPkg);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
