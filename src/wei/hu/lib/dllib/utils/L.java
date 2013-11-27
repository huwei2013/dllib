package wei.hu.lib.dllib.utils;

import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("NewApi")
public final class L {

	private static boolean loggable = true;

	private L() {

	}

	public static boolean isLoggable() {
		return loggable;
	}

	public static void setLoggable(boolean loggable) {
		L.loggable = loggable;
	}

	public int d(String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.d(tag, msg);
	}

	public int d(String tag, String msg, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.d(tag, msg, tr);
	}

	public int e(String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.e(tag, msg);
	}

	public int e(String tag, String msg, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.e(tag, msg, tr);
	}

	public String getStackTraceString(Throwable tr) {
		if (!isLoggable())
			return null;
		return Log.getStackTraceString(tr);
	}

	public int i(String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.i(tag, msg);
	}

	public int i(String tag, String msg, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.i(tag, msg, tr);
	}

	public boolean isLoggable(String tag, int level) {
		return Log.isLoggable(tag, level);
	}

	public int println(int priority, String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.println(priority, tag, msg);
	}

	public int v(String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.v(tag, msg);
	}

	public int v(String tag, String msg, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.v(tag, msg, tr);
	}

	public int w(String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.w(tag, msg);
	}

	public int w(String tag, String msg, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.w(tag, msg, tr);
	}

	public int w(String tag, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.w(tag, tr);
	}

	public int wtf(String tag, String msg) {
		if (!isLoggable())
			return -1;
		return Log.wtf(tag, msg);
	}

	public int wtf(String tag, String msg, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.wtf(tag, msg, tr);
	}

	public int wtf(String tag, Throwable tr) {
		if (!isLoggable())
			return -1;
		return Log.wtf(tag, tr);
	}
}
