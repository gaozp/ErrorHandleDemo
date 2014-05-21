package com.example.errorhandledemo.application;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;
import android.util.Log;

public class ErrorHandleDemoApplication extends Application {

	public static boolean isDebug = true;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e("gaozhipeng", "" + getApplicationDebuggable());
		if (getApplicationDebuggable()) {
			isDebug = true;
			openStrictMode();
		} else {
			isDebug = false;
			UncaughtExceptionHandler.getInstance()
					.init(getApplicationContext());
		}
	}

	private void openStrictMode() {
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		builder.detectAll();
		builder.penaltyLog();
		StrictMode.VmPolicy vmp = builder.build();
		StrictMode.setVmPolicy(vmp);
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
