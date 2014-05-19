package com.example.errorhandledemo.application;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

public class ErrorHandleDemoApplication extends Application {

	public static boolean isDebug = true;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("gaozhipeng", "" + getApplicationDebuggable());
		if (getApplicationDebuggable()) {
			isDebug = true;
		} else {
			isDebug = false;
			UncaughtExceptionHandler.getInstance()
					.init(getApplicationContext());
		}
	}

	/**
	 * 得到系统是否是debug模式
	 * 
	 * @return
	 */
	public boolean getApplicationDebuggable() {
		ApplicationInfo info = this.getApplicationInfo();
		if (info != null) {
			if ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
}
