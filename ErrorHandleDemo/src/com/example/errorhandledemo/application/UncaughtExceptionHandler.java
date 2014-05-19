package com.example.errorhandledemo.application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.errorhandledemo.MainActivity;

public class UncaughtExceptionHandler implements
		java.lang.Thread.UncaughtExceptionHandler {
	// 系统默认的uncaughtexception处理类
	private java.lang.Thread.UncaughtExceptionHandler mDefaultHandler;
	public static final String TAG = "CrashHandler";
	Context mContext;
	// 用来存储设备信息和异常信息
	private Map<String, String> infos = new HashMap<String, String>();
	// 用于格式化日期，作为日志文件名的一部分
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	private static UncaughtExceptionHandler mInstance = new UncaughtExceptionHandler();

	public static final String CRASH_PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/crash/";

	private UncaughtExceptionHandler() {

	}

	public static UncaughtExceptionHandler getInstance() {
		return mInstance;
	}

	public void init(Context context) {
		mContext = context;
		// 获取系统默认的异常处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 将该异常处理器设置为系统默认的处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable arg1) {
		if (!handleException(arg1) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(arg0, arg1);
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 重新启动应用
			Intent intent = new Intent(mContext, MainActivity.class);
			PendingIntent restartIntent = PendingIntent.getActivity(mContext,
					0, intent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
			AlarmManager mgr = (AlarmManager) mContext
					.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10,
					restartIntent); // 10毫秒钟后重启应用
			android.os.Process.killProcess(android.os.Process.myPid());
		}

	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		new Thread() {
			public void run() {
				Looper.prepare();
				Toast.makeText(mContext, "异常退出啦,正在重启应用。", Toast.LENGTH_LONG)
						.show();
				Looper.loop();
			};
		}.start();
		// 收集设备信息
		collectDeviceInfo(mContext);
		// 保存日志文件
		saveCrashInfo2File(ex);
		return true;
	}

	private String saveCrashInfo2File(Throwable ex) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				File dir = new File(CRASH_PATH);
				if (!dir.exists()) {
					boolean b = dir.mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(CRASH_PATH
						+ fileName);
				fos.write(sb.toString().getBytes());
				fos.close();
			}
			return sb.toString();
		} catch (Exception e) {
			Log.e(TAG, "an error occured while writing file...", e);
		}
		return null;

	}

	private void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null"
						: pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
	}

}
