package com.example.lammy.androidutils.statusBar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * 要使用systembartint 在gradle中配置下面库
 * compile 'com.readystatesoftware.systembartint:systembartint:1.0.3'
 */
public class NewStatusBarUtil {

	public static void quitFullScreen(Activity activity){
		Window window = activity.getWindow();
		final WindowManager.LayoutParams attrs = window.getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.setAttributes(attrs);
		window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
	}

	public static void setFullScreen(Activity activity){
		Window window = activity.getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	public static void setFullScreen2(Activity activity){
		if (Build.VERSION.SDK_INT >= 21) {
			View decorView = activity.getWindow().getDecorView();
			int option =  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
					| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			decorView.setSystemUiVisibility(option);
			activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
	}

	public static void setStatusBarColor(Activity activity, int statusColor, boolean bDark){
		Window window = activity.getWindow();
		final WindowManager.LayoutParams attrs = window.getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.setAttributes(attrs);
		//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		//设置状态栏颜色
		window.setStatusBarColor(statusColor);

		setDarkStatusIcon(window, bDark);
//		ViewGroup mContentView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
//		View mChildView = mContentView.getChildAt(0);
//		if (mChildView != null) {
//			//注意不是设置 ContentView 的 FitsSystemWindows,
//			// 而是设置 ContentView 的第一个子 View .
//			// 预留出系统 View 的空间.
//			ViewCompat.setFitsSystemWindows(mChildView, true);
//		}
	}

	public static void setStatusBarTransparent(Activity activity, boolean bDark ){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);

			setDarkStatusIcon(window, bDark);
		}


	}


	public static void setStatusBarColor(Window window, int statusColor, boolean bDark){
		final WindowManager.LayoutParams attrs = window.getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		window.setAttributes(attrs);
		//取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

		//需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		//设置状态栏颜色
		window.setStatusBarColor(statusColor);

		setDarkStatusIcon(window, bDark);
	}

	public static void setStatusBarColor(Activity activity, int statusColor){
		setStatusBarColor(activity, statusColor, true);
	}

	public static void setDarkStatusIcon(Window window, boolean bDark) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			View decorView = window.getDecorView();
			if(decorView != null){
				int vis = decorView.getSystemUiVisibility();
				if(bDark){
					vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
				} else{
					vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
				}
				decorView.setSystemUiVisibility(vis);
			}
		}
	}
	public static boolean checkDeviceHasNavigationBar(Activity context) {
		boolean hasNavigationBar = false;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				Display display = context.getWindowManager().getDefaultDisplay();
				Point size = new Point();
				Point realSize = new Point();
				display.getSize(size);
				display.getRealSize(realSize);
				hasNavigationBar =  realSize.y!=size.y;
			}else {
				boolean menu = ViewConfiguration.get(context).hasPermanentMenuKey();
				boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
				if(menu || back) {
					hasNavigationBar =  false;
				}else {
					hasNavigationBar =  true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasNavigationBar;
	}

	public static int getNavigationBarHeight(Activity context) {
		int result = 0;
		if (checkDeviceHasNavigationBar(context)) {
			Resources res = context.getResources();
			int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
			if (resourceId > 0) {
				result = res.getDimensionPixelSize(resourceId);
			}
		}
		return result;
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * 修改状态栏为全透明
	 *
	 * @param activity
	 */
	@TargetApi(19)
	public static void transparencyBar(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor(Color.TRANSPARENT);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window window = activity.getWindow();
			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}

	/**
	 * 修改状态栏颜色，支持4.4以上版本
	 *
	 * @param activity
	 * @param colorId
	 */
	public static void setStatusBarColor2(Activity activity, int colorId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			setTranslucentStatus(activity);
		}
		SystemBarTintManager tintManager = new SystemBarTintManager(activity);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setStatusBarTintResource(colorId);
	}

	@TargetApi(19)
	public static void setTranslucentStatus(Activity activity) {
		Window window = activity.getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
	}
}
